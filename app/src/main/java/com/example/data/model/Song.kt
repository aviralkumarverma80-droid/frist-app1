package com.example.data.model

import com.example.R

data class LyricLine(
    val timestampSec: Float,
    val text: String
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSec: Int,
    val genre: String,
    val coverResId: Int,
    val bpm: Int = 85,
    val rootNoteHz: Float = 220.0f, // A3
    val scaleType: String = "minor", // minor, pentatonic, major, dorian
    val melodyPattern: IntArray = intArrayOf(0, 3, 7, 10, 7, 5, 3, 0),
    val bassPattern: IntArray = intArrayOf(0, 0, 5, 5, 3, 3, 7, 7),
    val drumStyle: String = "chill", // chill, synthwave, acoustic, cyber
    val lyrics: List<LyricLine> = emptyList(),
    val isFavorite: Boolean = false
) {
    val durationFormatted: String
        get() {
            val minutes = durationSec / 60
            val seconds = durationSec % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    companion object {
        val SAMPLE_SONGS = listOf(
            Song(
                id = "track_1",
                title = "Midnight Coffee",
                artist = "Aura Bloom",
                album = "Rainy Street Sessions",
                durationSec = 194,
                genre = "Lo-Fi Chill",
                coverResId = R.drawable.img_cover_lofi,
                bpm = 78,
                rootNoteHz = 196.0f, // G3
                scaleType = "pentatonic",
                melodyPattern = intArrayOf(0, 3, 5, 7, 10, 7, 5, 3, 2, 5, 7, 10),
                bassPattern = intArrayOf(0, -5, -2, -7),
                drumStyle = "chill",
                lyrics = listOf(
                    LyricLine(0f, "♪ Rainy whispers against the glass..."),
                    LyricLine(12f, "Steam rising from a quiet cup of tea"),
                    LyricLine(25f, "Soft streetlights blurring in the haze"),
                    LyricLine(40f, "Time slows down in midnight serenity"),
                    LyricLine(56f, "Notes drifting through open spaces"),
                    LyricLine(74f, "Just you, the melody, and the gentle rain"),
                    LyricLine(95f, "Fading shadows, steady rhythmic heartbeat"),
                    LyricLine(120f, "Peace found in quiet solitude")
                )
            ),
            Song(
                id = "track_2",
                title = "Neon Skyline 1984",
                artist = "HyperDrive",
                album = "Retrowave Horizon",
                durationSec = 228,
                genre = "Synthwave",
                coverResId = R.drawable.img_cover_synth,
                bpm = 120,
                rootNoteHz = 130.81f, // C3
                scaleType = "minor",
                melodyPattern = intArrayOf(0, 7, 3, 7, 0, 10, 7, 3, 2, 5, 8, 7),
                bassPattern = intArrayOf(0, 0, 3, 3, -2, -2, 5, 5),
                drumStyle = "synthwave",
                lyrics = listOf(
                    LyricLine(0f, "★ Electric engines igniting the dusk..."),
                    LyricLine(15f, "Cruising down the endless grid highway"),
                    LyricLine(32f, "Magenta neon reflections on chrome"),
                    LyricLine(48f, "Faster than yesterday, chasing the sunrise"),
                    LyricLine(65f, "Feel the bass resonance in your soul"),
                    LyricLine(88f, "Synthetic dreams running through the wire"),
                    LyricLine(115f, "We are the midnight runners of tomorrow"),
                    LyricLine(145f, "Never looking back in the rearview mirror")
                )
            ),
            Song(
                id = "track_3",
                title = "Golden Meadow",
                artist = "Canyon & Pine",
                album = "Sunlight on Timber",
                durationSec = 210,
                genre = "Acoustic Folk",
                coverResId = R.drawable.img_cover_acoustic,
                bpm = 92,
                rootNoteHz = 146.83f, // D3
                scaleType = "major",
                melodyPattern = intArrayOf(0, 4, 7, 12, 11, 7, 4, 2, 4, 7, 9, 7),
                bassPattern = intArrayOf(0, 0, 4, 4, 5, 5, 2, 2),
                drumStyle = "acoustic",
                lyrics = listOf(
                    LyricLine(0f, "☼ Warm breeze sweeping through high grass..."),
                    LyricLine(14f, "Woodsmoke drifting above the valley floor"),
                    LyricLine(29f, "Dust motes dancing in amber sunlight rays"),
                    LyricLine(46f, "Simple chords echoing against the pines"),
                    LyricLine(66f, "Walking home with nothing left to prove"),
                    LyricLine(88f, "The river carries every worried thought away"),
                    LyricLine(112f, "Safe in the shelter of the timber trees"),
                    LyricLine(140f, "Golden hour never has to end")
                )
            ),
            Song(
                id = "track_4",
                title = "Cybernetic Pulse",
                artist = "Vortex Protocol",
                album = "Subterranean Frequency",
                durationSec = 245,
                genre = "Cyber Electronic",
                coverResId = R.drawable.img_cover_cyber,
                bpm = 128,
                rootNoteHz = 110.0f, // A2
                scaleType = "dorian",
                melodyPattern = intArrayOf(0, 12, 10, 7, 12, 15, 12, 10, 7, 5, 3, 5),
                bassPattern = intArrayOf(0, 0, -5, -5, 7, 7, 5, 5),
                drumStyle = "cyber",
                lyrics = listOf(
                    LyricLine(0f, "⚡ Initializing sub-bass oscillators..."),
                    LyricLine(16f, "Data packet streams surging through the veins"),
                    LyricLine(34f, "Sub-harmonic pressure bending light"),
                    LyricLine(52f, "Synchronized kinetic movement on the floor"),
                    LyricLine(72f, "128 BPM resonance lock established"),
                    LyricLine(96f, "Digital euphoria taking over control"),
                    LyricLine(124f, "Breakdown drops into infinite sub-reverb"),
                    LyricLine(160f, "Rebuilding pulse to maximum amplitude")
                )
            ),
            Song(
                id = "track_5",
                title = "Velvet Midnight",
                artist = "Luna Trio",
                album = "Dimly Lit Room",
                durationSec = 182,
                genre = "Lo-Fi Chill",
                coverResId = R.drawable.img_cover_lofi,
                bpm = 72,
                rootNoteHz = 174.61f, // F3
                scaleType = "pentatonic",
                melodyPattern = intArrayOf(0, 3, 5, 7, 10, 12, 10, 7, 5, 3, 0, 2),
                bassPattern = intArrayOf(0, -4, -2, -6),
                drumStyle = "chill",
                lyrics = listOf(
                    LyricLine(0f, "☕ Low tape hiss spinning on the deck..."),
                    LyricLine(18f, "Shadows stretching along quiet walls"),
                    LyricLine(36f, "Electric piano chime lingering in air"),
                    LyricLine(55f, "A late-night thought floating untamed"),
                    LyricLine(80f, "Gentle warm bass comforting the room"),
                    LyricLine(110f, "Sip the warmth before the night moves on")
                )
            ),
            Song(
                id = "track_6",
                title = "Starlight Highway",
                artist = "HyperDrive",
                album = "Retrowave Horizon",
                durationSec = 215,
                genre = "Synthwave",
                coverResId = R.drawable.img_cover_synth,
                bpm = 116,
                rootNoteHz = 164.81f, // E3
                scaleType = "minor",
                melodyPattern = intArrayOf(0, 3, 7, 12, 10, 7, 3, 2, 3, 5, 7, 3),
                bassPattern = intArrayOf(0, 0, -4, -4, 3, 3, 5, 5),
                drumStyle = "synthwave",
                lyrics = listOf(
                    LyricLine(0f, "✦ Constellations racing overhead..."),
                    LyricLine(16f, "Tachometer glowing orange in the dark"),
                    LyricLine(35f, "Arpeggiator painting stars across the sky"),
                    LyricLine(58f, "Speed of light wrapped in synthesizer chords"),
                    LyricLine(82f, "Into the electric infinite we ride"),
                    LyricLine(115f, "The road is our universe tonight")
                )
            )
        )
    }
}
