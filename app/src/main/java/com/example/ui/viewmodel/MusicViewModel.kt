package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackEngine
import com.example.data.local.MusicDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.model.EqPreset
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class MusicTab {
    TRACKS, PLAYLISTS, FAVORITES, EQUALIZER
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MusicRepository
    private val audioEngine = AudioPlaybackEngine()

    val allSongs: StateFlow<List<Song>>
    val favoriteSongs: StateFlow<List<Song>>
    val playlists: StateFlow<List<PlaylistEntity>>
    val recentSongs: StateFlow<List<Song>>

    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentPositionSec: StateFlow<Float> = audioEngine.currentPositionSec
    val durationSec: StateFlow<Float> = audioEngine.durationSec
    val visualizerBands: StateFlow<FloatArray> = audioEngine.visualizerBands
    val currentSong: StateFlow<Song?> = audioEngine.currentSong

    private val _queue = MutableStateFlow<List<Song>>(Song.SAMPLE_SONGS)
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _currentEqPreset = MutableStateFlow(EqPreset.ALL_PRESETS[0])
    val currentEqPreset: StateFlow<EqPreset> = _currentEqPreset.asStateFlow()

    private val _bassBoost = MutableStateFlow(0.3f)
    val bassBoost: StateFlow<Float> = _bassBoost.asStateFlow()

    private val _trebleBoost = MutableStateFlow(0.2f)
    val trebleBoost: StateFlow<Float> = _trebleBoost.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    private val _isFullPlayerOpen = MutableStateFlow(false)
    val isFullPlayerOpen: StateFlow<Boolean> = _isFullPlayerOpen.asStateFlow()

    private val _activeTab = MutableStateFlow(MusicTab.TRACKS)
    val activeTab: StateFlow<MusicTab> = _activeTab.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    private val _playlistSongs = MutableStateFlow<List<Song>>(emptyList())
    val playlistSongs: StateFlow<List<Song>> = _playlistSongs.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _songToAddToPlaylist = MutableStateFlow<Song?>(null)
    val songToAddToPlaylist: StateFlow<Song?> = _songToAddToPlaylist.asStateFlow()

    // Filtered songs according to search query and selected genre
    val filteredSongs: StateFlow<List<Song>>

    init {
        val db = MusicDatabase.getDatabase(application)
        repository = MusicRepository(db.musicDao())

        allSongs = repository.allSongs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Song.SAMPLE_SONGS
        )

        favoriteSongs = repository.favoriteSongs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        playlists = repository.playlists.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        recentSongs = repository.recentSongs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        filteredSongs = combine(allSongs, _searchQuery, _selectedGenre) { songs, query, genre ->
            songs.filter { song ->
                val matchesQuery = query.isBlank() ||
                        song.title.contains(query, ignoreCase = true) ||
                        song.artist.contains(query, ignoreCase = true) ||
                        song.album.contains(query, ignoreCase = true)
                val matchesGenre = genre == null || song.genre.equals(genre, ignoreCase = true)
                matchesQuery && matchesGenre
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Song.SAMPLE_SONGS
        )

        // Setup engine completion callback
        audioEngine.onSongCompleted = {
            viewModelScope.launch {
                handleTrackFinished()
            }
        }

        // Apply initial EQ
        audioEngine.setCustomBassAndTreble(_bassBoost.value, _trebleBoost.value)
    }

    fun playSong(song: Song, newQueue: List<Song>? = null) {
        val targetQueue = newQueue ?: if (_queue.value.any { it.id == song.id }) _queue.value else listOf(song) + _queue.value
        _queue.value = targetQueue
        val idx = targetQueue.indexOfFirst { it.id == song.id }
        _queueIndex.value = if (idx >= 0) idx else 0

        audioEngine.play(song, 0f)
        viewModelScope.launch {
            repository.recordPlayback(song.id)
        }
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            audioEngine.pause()
        } else {
            if (currentSong.value == null) {
                val first = _queue.value.firstOrNull() ?: Song.SAMPLE_SONGS.first()
                playSong(first)
            } else {
                audioEngine.resume()
            }
        }
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        val current = currentSong.value
        if (_repeatMode.value == RepeatMode.ONE && current != null) {
            audioEngine.seekTo(0f)
            audioEngine.play(current, 0f)
            return
        }

        val nextIdx = if (_isShuffle.value) {
            (q.indices).random()
        } else {
            (_queueIndex.value + 1) % q.size
        }

        _queueIndex.value = nextIdx
        audioEngine.play(q[nextIdx], 0f)
        viewModelScope.launch {
            repository.recordPlayback(q[nextIdx].id)
        }
    }

    fun playPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        // If played more than 3 seconds, replay current song
        if (currentPositionSec.value > 3f) {
            audioEngine.seekTo(0f)
            return
        }

        val prevIdx = if (_isShuffle.value) {
            (q.indices).random()
        } else {
            if (_queueIndex.value - 1 < 0) q.size - 1 else _queueIndex.value - 1
        }

        _queueIndex.value = prevIdx
        audioEngine.play(q[prevIdx], 0f)
        viewModelScope.launch {
            repository.recordPlayback(q[prevIdx].id)
        }
    }

    fun seekTo(seconds: Float) {
        audioEngine.seekTo(seconds)
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        audioEngine.setSpeed(speed)
    }

    fun setEqPreset(preset: EqPreset) {
        _currentEqPreset.value = preset
        _bassBoost.value = preset.bassBoost
        _trebleBoost.value = preset.trebleBoost
        audioEngine.setEqPreset(preset)
    }

    fun setBassBoost(level: Float) {
        _bassBoost.value = level
        audioEngine.setCustomBassAndTreble(_bassBoost.value, _trebleBoost.value)
    }

    fun setTrebleBoost(level: Float) {
        _trebleBoost.value = level
        audioEngine.setCustomBassAndTreble(_bassBoost.value, _trebleBoost.value)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedGenre(genre: String?) {
        _selectedGenre.value = genre
    }

    fun setFullPlayerOpen(open: Boolean) {
        _isFullPlayerOpen.value = open
    }

    fun setActiveTab(tab: MusicTab) {
        _activeTab.value = tab
    }

    fun openPlaylist(playlist: PlaylistEntity) {
        _selectedPlaylist.value = playlist
        viewModelScope.launch {
            repository.getSongsForPlaylist(playlist.id).collect { songs ->
                _playlistSongs.value = songs
            }
        }
    }

    fun closePlaylist() {
        _selectedPlaylist.value = null
        _playlistSongs.value = emptyList()
    }

    fun createPlaylist(name: String, description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name.trim(), description.trim())
            _showCreatePlaylistDialog.value = false
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist.id)
            if (_selectedPlaylist.value?.id == playlist.id) {
                closePlaylist()
            }
        }
    }

    fun showCreatePlaylistDialog(show: Boolean) {
        _showCreatePlaylistDialog.value = show
    }

    fun setSongToAddToPlaylist(song: Song?) {
        _songToAddToPlaylist.value = song
    }

    fun addSongToPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            _songToAddToPlaylist.value = null
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    private fun handleTrackFinished() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                val current = currentSong.value ?: return
                audioEngine.play(current, 0f)
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                if (_queueIndex.value + 1 < _queue.value.size) {
                    playNext()
                } else {
                    audioEngine.pause()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
