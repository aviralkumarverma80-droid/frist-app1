package com.example.data.model

data class EqPreset(
    val id: String,
    val name: String,
    val bands: List<Float>, // 5 frequency bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz (values -10f to +10f)
    val bassBoost: Float = 0f, // 0f to 1f
    val trebleBoost: Float = 0f // 0f to 1f
) {
    companion object {
        val ALL_PRESETS = listOf(
            EqPreset(
                id = "flat",
                name = "Flat (Neutral)",
                bands = listOf(0f, 0f, 0f, 0f, 0f),
                bassBoost = 0f,
                trebleBoost = 0f
            ),
            EqPreset(
                id = "bass_boost",
                name = "Bass Boost",
                bands = listOf(7f, 5f, 1f, -1f, -2f),
                bassBoost = 0.8f,
                trebleBoost = 0.1f
            ),
            EqPreset(
                id = "electronic",
                name = "Electronic / EDM",
                bands = listOf(6f, 3f, -1f, 4f, 6f),
                bassBoost = 0.6f,
                trebleBoost = 0.5f
            ),
            EqPreset(
                id = "acoustic",
                name = "Acoustic / Folk",
                bands = listOf(3f, 2f, 4f, 5f, 3f),
                bassBoost = 0.2f,
                trebleBoost = 0.4f
            ),
            EqPreset(
                id = "vocal",
                name = "Vocal Clarity",
                bands = listOf(-2f, 1f, 6f, 5f, 2f),
                bassBoost = 0.1f,
                trebleBoost = 0.5f
            ),
            EqPreset(
                id = "lofi",
                name = "Lo-Fi Warmth",
                bands = listOf(5f, 4f, 2f, -3f, -5f),
                bassBoost = 0.5f,
                trebleBoost = 0.0f
            )
        )
    }
}
