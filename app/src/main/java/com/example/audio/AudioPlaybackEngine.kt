package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.model.EqPreset
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlaybackEngine {

    private val sampleRate = 22050
    private val synth = ProceduralMusicSynth(sampleRate)

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionSec = MutableStateFlow(0f)
    val currentPositionSec: StateFlow<Float> = _currentPositionSec.asStateFlow()

    private val _durationSec = MutableStateFlow(0f)
    val durationSec: StateFlow<Float> = _durationSec.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _visualizerBands = MutableStateFlow(FloatArray(16) { 0.1f })
    val visualizerBands: StateFlow<FloatArray> = _visualizerBands.asStateFlow()

    private var activeEqPreset: EqPreset = EqPreset.ALL_PRESETS[0]
    private var activeBassBoost: Float = 0f
    private var activeTrebleBoost: Float = 0f
    private var activeSpeed: Float = 1.0f

    var onSongCompleted: (() -> Unit)? = null

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = (minBufferSize * 2).coerceAtLeast(sampleRate / 4)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        audioTrack = AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM,
            android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }

    fun play(song: Song, startPositionSec: Float = 0f) {
        playbackJob?.cancel()
        _currentSong.value = song
        _currentPositionSec.value = startPositionSec
        _durationSec.value = song.durationSec.toFloat()

        try {
            if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                initAudioTrack()
            }
            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _isPlaying.value = true

        playbackJob = scope.launch {
            val chunkDurationSec = 0.08f // 80ms chunks
            var curTime = startPositionSec

            while (isActive && _isPlaying.value) {
                if (curTime >= song.durationSec) {
                    _isPlaying.value = false
                    _currentPositionSec.value = song.durationSec.toFloat()
                    onSongCompleted?.invoke()
                    break
                }

                val samples = synth.renderChunk(
                    song = song,
                    startSec = curTime,
                    durationSec = chunkDurationSec,
                    eqPreset = activeEqPreset,
                    bassBoostFactor = activeBassBoost,
                    trebleBoostFactor = activeTrebleBoost,
                    speed = activeSpeed
                )

                audioTrack?.write(samples, 0, samples.size)

                // Update visualizer
                val bands = synth.extractVisualizerBands(samples)
                _visualizerBands.value = bands

                curTime += chunkDurationSec * activeSpeed
                _currentPositionSec.value = curTime
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        try {
            audioTrack?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Idle visualizer bars
        _visualizerBands.value = FloatArray(16) { 0.08f }
    }

    fun resume() {
        val song = _currentSong.value ?: return
        play(song, _currentPositionSec.value)
    }

    fun seekTo(positionSec: Float) {
        val song = _currentSong.value ?: return
        val clamped = positionSec.coerceIn(0f, song.durationSec.toFloat())
        _currentPositionSec.value = clamped
        if (_isPlaying.value) {
            play(song, clamped)
        }
    }

    fun setSpeed(speed: Float) {
        activeSpeed = speed.coerceIn(0.5f, 2.0f)
    }

    fun setEqPreset(preset: EqPreset) {
        activeEqPreset = preset
        activeBassBoost = preset.bassBoost
        activeTrebleBoost = preset.trebleBoost
    }

    fun setCustomBassAndTreble(bass: Float, treble: Float) {
        activeBassBoost = bass.coerceIn(0f, 1f)
        activeTrebleBoost = treble.coerceIn(0f, 1f)
    }

    fun release() {
        playbackJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
    }
}
