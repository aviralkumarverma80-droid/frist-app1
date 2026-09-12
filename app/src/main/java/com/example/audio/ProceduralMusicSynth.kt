package com.example.audio

import com.example.data.model.EqPreset
import com.example.data.model.Song
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Real-time procedural synthesizer for music tracks.
 * Generates multi-timbral polyphonic audio: Lead melody, Harmony/Chords, Bassline, and Percussion.
 */
class ProceduralMusicSynth(
    val sampleRate: Int = 22050
) {
    private val random = Random(42)

    // Semitone ratios relative to root note
    private fun semitoneToFreq(rootHz: Float, semitone: Int): Float {
        return (rootHz * Math.pow(2.0, semitone / 12.0)).toFloat()
    }

    /**
     * Synthesizes audio PCM 16-bit samples for a given chunk of time.
     * @param song The song parameters
     * @param startSec Elapsed time in seconds
     * @param durationSec Duration of audio chunk in seconds
     * @param eqPreset User selected equalizer preset
     * @param bassBoostFactor 0.0 to 1.0
     * @param trebleBoostFactor 0.0 to 1.0
     * @param speed Playback speed multiplier (0.75 to 1.5)
     * @return ShortArray containing 16-bit PCM samples
     */
    fun renderChunk(
        song: Song,
        startSec: Float,
        durationSec: Float,
        eqPreset: EqPreset,
        bassBoostFactor: Float,
        trebleBoostFactor: Float,
        speed: Float
    ): ShortArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        if (numSamples <= 0) return buffer

        val effectiveBpm = (song.bpm * speed).coerceIn(40f, 220f)
        val beatsPerSec = effectiveBpm / 60.0f
        val sixteenthNoteSec = 1.0f / (beatsPerSec * 4.0f)

        // Equalizer weights
        val bassGain = 1.0f + (bassBoostFactor * 1.5f) + (eqPreset.bands[0] / 15f)
        val midGain = 1.0f + (eqPreset.bands[2] / 20f)
        val highGain = 1.0f + (trebleBoostFactor * 1.5f) + (eqPreset.bands[4] / 15f)

        for (i in 0 until numSamples) {
            val t = startSec + (i.toFloat() / sampleRate)

            // Calculate current 16th-note step in song
            val step = (t / sixteenthNoteSec).toLong()
            val stepFraction = ((t % sixteenthNoteSec) / sixteenthNoteSec)

            // 1. Bassline (8-step loop)
            val bassStepIndex = ((step / 2) % song.bassPattern.size.coerceAtLeast(1)).toInt()
            val bassSemitone = song.bassPattern[bassStepIndex]
            val bassFreq = semitoneToFreq(song.rootNoteHz * 0.5f, bassSemitone)
            val bassEnv = exp(-stepFraction * 3.5f) // pluck decay
            val bassSample = sin(2.0 * PI * bassFreq * t).toFloat() * bassEnv * bassGain * 0.35f

            // 2. Melody Lead (16-step loop)
            val melodyStepIndex = (step % song.melodyPattern.size.coerceAtLeast(1)).toInt()
            val melodySemitone = song.melodyPattern[melodyStepIndex]
            val melodyFreq = semitoneToFreq(song.rootNoteHz * 2.0f, melodySemitone)
            val melodyEnv = (1.0f - stepFraction).coerceIn(0f, 1f) * exp(-stepFraction * 2.0f)
            // Harmonic overtone for bell/synth quality
            val melodySample = (sin(2.0 * PI * melodyFreq * t) * 0.7f +
                    sin(4.0 * PI * melodyFreq * t) * 0.25f +
                    sin(6.0 * PI * melodyFreq * t) * 0.05f).toFloat() * melodyEnv * midGain * 0.25f

            // 3. Harmony Pad / Chord progression
            val chordStep = ((step / 16) % 4).toInt()
            val chordRootSemitone = when (song.scaleType) {
                "minor" -> when (chordStep) { 0 -> 0; 1 -> 3; 2 -> -2; else -> 5 }
                "major" -> when (chordStep) { 0 -> 0; 1 -> 5; 2 -> 7; else -> 4 }
                else -> when (chordStep) { 0 -> 0; 1 -> 7; 2 -> 5; else -> 3 }
            }
            val chordFreq1 = semitoneToFreq(song.rootNoteHz, chordRootSemitone)
            val chordFreq2 = semitoneToFreq(song.rootNoteHz, chordRootSemitone + 7)
            val padSample = (sin(2.0 * PI * chordFreq1 * t) * 0.5f +
                    sin(2.0 * PI * chordFreq2 * t) * 0.5f).toFloat() * 0.12f * midGain

            // 4. Drums / Percussion
            var drumSample = 0.0f
            val beatStep = (step % 16).toInt()
            // Kick on 0, 8 (or 0, 6, 10 for breakbeat)
            val isKick = when (song.drumStyle) {
                "cyber" -> beatStep == 0 || beatStep == 4 || beatStep == 8 || beatStep == 12 // 4-on-floor
                "synthwave" -> beatStep == 0 || beatStep == 6 || beatStep == 10
                else -> beatStep == 0 || beatStep == 8
            }
            if (isKick) {
                val kickTime = stepFraction * sixteenthNoteSec
                val kickPitch = (140.0f * exp(-kickTime * 45.0f)).coerceAtLeast(42f)
                drumSample += (sin(2.0 * PI * kickPitch * t).toFloat() * exp(-kickTime * 28.0f)) * 0.45f * bassGain
            }

            // Snare on 4, 12
            if (beatStep == 4 || beatStep == 12) {
                val snareTime = stepFraction * sixteenthNoteSec
                val noise = (random.nextFloat() * 2f - 1f)
                val tone = sin(2.0 * PI * 180.0 * t).toFloat() * 0.3f
                drumSample += ((noise * 0.7f + tone) * exp(-snareTime * 35.0f)) * 0.30f * midGain
            }

            // Hi-Hat on even 16th steps
            if (beatStep % 2 == 0) {
                val hatTime = stepFraction * sixteenthNoteSec
                val noise = (random.nextFloat() * 2f - 1f)
                drumSample += (noise * exp(-hatTime * 120.0f)) * 0.12f * highGain
            }

            // Mix channels
            val mixed = bassSample + melodySample + padSample + drumSample
            // Soft clipping limiter
            val clamped = (mixed * 0.75f).coerceIn(-1.0f, 1.0f)
            buffer[i] = (clamped * 32767.0f).toInt().toShort()
        }

        return buffer
    }

    /**
     * Compute approximate 16-band frequency visualizer magnitudes from recent samples.
     */
    fun extractVisualizerBands(buffer: ShortArray): FloatArray {
        val bands = FloatArray(16)
        if (buffer.isEmpty()) return bands

        val bandSize = buffer.size / 16
        if (bandSize <= 0) return bands

        for (b in 0 until 16) {
            var sum = 0.0
            val start = b * bandSize
            val end = (start + bandSize).coerceAtMost(buffer.size)
            for (j in start until end) {
                val norm = buffer[j].toDouble() / 32768.0
                sum += abs(norm)
            }
            val avg = (sum / (end - start)).toFloat()
            // Dynamic non-linear scaling for responsive visualizer bars
            bands[b] = (avg * 2.2f).coerceIn(0.08f, 1.0f)
        }
        return bands
    }
}
