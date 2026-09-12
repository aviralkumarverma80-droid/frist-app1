package com.example.data.repository

import com.example.data.local.FavoriteSongEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.MusicDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongEntity
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MusicRepository(private val musicDao: MusicDao) {

    private val baseSongs = Song.SAMPLE_SONGS

    val allSongs: Flow<List<Song>> = musicDao.getAllFavoriteSongIds().map { favoriteIds ->
        val favSet = favoriteIds.toSet()
        baseSongs.map { song ->
            song.copy(isFavorite = favSet.contains(song.id))
        }
    }

    val favoriteSongs: Flow<List<Song>> = musicDao.getAllFavoriteSongIds().map { favoriteIds ->
        val favSet = favoriteIds.toSet()
        baseSongs.filter { favSet.contains(it.id) }.map { it.copy(isFavorite = true) }
    }

    val playlists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()

    val recentSongs: Flow<List<Song>> = combine(
        musicDao.getRecentHistorySongIds(),
        musicDao.getAllFavoriteSongIds()
    ) { historyIds, favIds ->
        val favSet = favIds.toSet()
        val songMap = baseSongs.associateBy { it.id }
        historyIds.mapNotNull { songMap[it]?.copy(isFavorite = favSet.contains(it)) }
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return combine(
            musicDao.getSongIdsForPlaylist(playlistId),
            musicDao.getAllFavoriteSongIds()
        ) { songIds, favIds ->
            val favSet = favIds.toSet()
            val songMap = baseSongs.associateBy { it.id }
            songIds.mapNotNull { songMap[it]?.copy(isFavorite = favSet.contains(it)) }
        }
    }

    suspend fun toggleFavorite(songId: String, currentFavorite: Boolean) {
        if (currentFavorite) {
            musicDao.removeFavorite(songId)
        } else {
            musicDao.addFavorite(FavoriteSongEntity(songId))
        }
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long {
        return musicDao.insertPlaylist(PlaylistEntity(name = name, description = description))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        musicDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        musicDao.addSongToPlaylist(PlaylistSongEntity(playlistId = playlistId, songId = songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun recordPlayback(songId: String) {
        musicDao.recordPlayback(HistoryEntity(songId = songId))
    }
}
