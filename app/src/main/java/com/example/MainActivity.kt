package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.FullPlayerSheet
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MusicTab
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MusicApp()
            }
        }
    }
}

@Composable
fun MusicApp(viewModel: MusicViewModel = viewModel()) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val filteredSongs by viewModel.filteredSongs.collectAsStateWithLifecycle()
    val recentSongs by viewModel.recentSongs.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionSec by viewModel.currentPositionSec.collectAsStateWithLifecycle()
    val durationSec by viewModel.durationSec.collectAsStateWithLifecycle()
    val visualizerBands by viewModel.visualizerBands.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val isFullPlayerOpen by viewModel.isFullPlayerOpen.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()

    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val playlistSongs by viewModel.playlistSongs.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()

    val currentEqPreset by viewModel.currentEqPreset.collectAsStateWithLifecycle()
    val bassBoost by viewModel.bassBoost.collectAsStateWithLifecycle()
    val trebleBoost by viewModel.trebleBoost.collectAsStateWithLifecycle()

    val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsStateWithLifecycle()
    val songToAddToPlaylist by viewModel.songToAddToPlaylist.collectAsStateWithLifecycle()

    // Handle back button when full player or playlist detail is open
    BackHandler(enabled = isFullPlayerOpen || selectedPlaylist != null) {
        if (isFullPlayerOpen) {
            viewModel.setFullPlayerOpen(false)
        } else if (selectedPlaylist != null) {
            viewModel.closePlaylist()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBackground,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Mini Player Bar docked above navigation bar
                    MiniPlayer(
                        song = currentSong,
                        isPlaying = isPlaying,
                        currentPositionSec = currentPositionSec,
                        durationSec = durationSec,
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onNextClick = { viewModel.playNext() },
                        onExpandClick = { viewModel.setFullPlayerOpen(true) }
                    )

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = activeTab == MusicTab.TRACKS,
                            onClick = { viewModel.setActiveTab(MusicTab.TRACKS) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = stringResource(R.string.tab_tracks)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_tracks)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_tracks")
                        )

                        NavigationBarItem(
                            selected = activeTab == MusicTab.PLAYLISTS,
                            onClick = { viewModel.setActiveTab(MusicTab.PLAYLISTS) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = stringResource(R.string.tab_playlists)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_playlists)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_playlists")
                        )

                        NavigationBarItem(
                            selected = activeTab == MusicTab.FAVORITES,
                            onClick = { viewModel.setActiveTab(MusicTab.FAVORITES) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorites"
                                )
                            },
                            label = { Text("Favorites") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_favorites")
                        )

                        NavigationBarItem(
                            selected = activeTab == MusicTab.EQUALIZER,
                            onClick = { viewModel.setActiveTab(MusicTab.EQUALIZER) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = stringResource(R.string.tab_sound)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_sound)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_equalizer")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                when (activeTab) {
                    MusicTab.TRACKS -> {
                        HomeScreen(
                            songs = filteredSongs,
                            recentSongs = recentSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            searchQuery = searchQuery,
                            selectedGenre = selectedGenre,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onGenreSelect = { viewModel.setSelectedGenre(it) },
                            onSongClick = { viewModel.playSong(it, filteredSongs) },
                            onFavoriteClick = { viewModel.toggleFavorite(it) },
                            onAddToPlaylistClick = { viewModel.setSongToAddToPlaylist(it) }
                        )
                    }

                    MusicTab.PLAYLISTS -> {
                        PlaylistsScreen(
                            playlists = playlists,
                            selectedPlaylist = selectedPlaylist,
                            playlistSongs = playlistSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onPlaylistClick = { viewModel.openPlaylist(it) },
                            onBackClick = { viewModel.closePlaylist() },
                            onCreatePlaylistClick = { viewModel.showCreatePlaylistDialog(true) },
                            onDeletePlaylistClick = { viewModel.deletePlaylist(it) },
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onRemoveSongClick = { song ->
                                selectedPlaylist?.let { pl ->
                                    viewModel.removeSongFromPlaylist(pl.id, song.id)
                                }
                            }
                        )
                    }

                    MusicTab.FAVORITES -> {
                        FavoritesScreen(
                            favoriteSongs = favoriteSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onRemoveFavoriteClick = { viewModel.toggleFavorite(it) }
                        )
                    }

                    MusicTab.EQUALIZER -> {
                        EqualizerScreen(
                            currentPreset = currentEqPreset,
                            bassBoost = bassBoost,
                            trebleBoost = trebleBoost,
                            visualizerBands = visualizerBands,
                            isPlaying = isPlaying,
                            onPresetSelect = { viewModel.setEqPreset(it) },
                            onBassBoostChange = { viewModel.setBassBoost(it) },
                            onTrebleBoostChange = { viewModel.setTrebleBoost(it) }
                        )
                    }
                }
            }
        }

        // Full Screen Player Animated Overlay
        AnimatedVisibility(
            visible = isFullPlayerOpen && currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            currentSong?.let { song ->
                FullPlayerSheet(
                    song = song,
                    isPlaying = isPlaying,
                    currentPositionSec = currentPositionSec,
                    durationSec = durationSec,
                    visualizerBands = visualizerBands,
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    playbackSpeed = playbackSpeed,
                    queue = queue,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onNextClick = { viewModel.playNext() },
                    onPrevClick = { viewModel.playPrevious() },
                    onSeek = { viewModel.seekTo(it) },
                    onShuffleClick = { viewModel.toggleShuffle() },
                    onRepeatClick = { viewModel.toggleRepeatMode() },
                    onFavoriteClick = { viewModel.toggleFavorite(song) },
                    onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                    onSelectFromQueue = { viewModel.playSong(it) },
                    onDismiss = { viewModel.setFullPlayerOpen(false) }
                )
            }
        }

        // Create Playlist Dialog
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { viewModel.showCreatePlaylistDialog(false) },
                onConfirm = { name, desc ->
                    viewModel.createPlaylist(name, desc)
                }
            )
        }

        // Add to Playlist Dialog
        songToAddToPlaylist?.let { song ->
            AddToPlaylistDialog(
                song = song,
                playlists = playlists,
                onDismiss = { viewModel.setSongToAddToPlaylist(null) },
                onPlaylistSelected = { playlistId ->
                    viewModel.addSongToPlaylist(playlistId, song.id)
                },
                onCreateNewPlaylist = {
                    viewModel.setSongToAddToPlaylist(null)
                    viewModel.showCreatePlaylistDialog(true)
                }
            )
        }
    }
}

// Retain Greeting for screenshot/unit tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
