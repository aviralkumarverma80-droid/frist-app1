package com.example

import com.example.audio.ProceduralMusicSynth
import com.example.data.model.EqPreset
import com.example.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicUnitTest {

    @Test
    fun sampleSongsCatalog_isNotEmpty() {
        val songs = Song.SAMPLE_SONGS
        assertTrue("Songs catalog should contain sample songs", songs.isNotEmpty())
        songs.forEach { song ->
            assertTrue("Song title must not be blank", song.title.isNotBlank())
            assertTrue("Song duration must be positive", song.durationSec > 0)
            assertNotNull("Song must have valid genre", song.genre)
        }
    }

    @Test
    fun eqPresets_hasDefaultPresets() {
        val presets = EqPreset.ALL_PRESETS
        assertTrue(presets.size >= 5)
        val flatPreset = presets.firstOrNull { it.id == "flat" }
        assertNotNull(flatPreset)
        assertEquals(5, flatPreset?.bands?.size)
    }

    @Test
    fun synth_rendersPcmChunkCorrectly() {
        val synth = ProceduralMusicSynth(22050)
        val song = Song.SAMPLE_SONGS.first()
        val pcmChunk = synth.renderChunk(
            song = song,
            startSec = 0f,
            durationSec = 0.1f,
            eqPreset = EqPreset.ALL_PRESETS[0],
            bassBoostFactor = 0.2f,
            trebleBoostFactor = 0.2f,
            speed = 1.0f
        )
        assertTrue(pcmChunk.isNotEmpty())
        assertEquals(2205, pcmChunk.size)

        val bands = synth.extractVisualizerBands(pcmChunk)
        assertEquals(16, bands.size)
    }
}
