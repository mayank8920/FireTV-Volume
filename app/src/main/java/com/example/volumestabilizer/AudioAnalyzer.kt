package com.example.volumestabilizer

import android.media.audiofx.Visualizer
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.log10
import kotlin.math.sqrt

class AudioAnalyzer(private val volumeController: VolumeController) {
    private var visualizer: Visualizer? = null
    private var isAnalyzing = false

    // Thresholds: Visualizer outputs arbitrary waveform amplitudes, not true acoustic dB.
    // You will need to heavily tune these values during testing.
    private val loudThreshold = 60.0 
    private val quietThreshold = 15.0

    fun startAnalysis() {
        if (isAnalyzing) return

        try {
            // Attach to Audio Session 0 (Global System Output Mix)
            visualizer = Visualizer(0).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1] // Max capture size
                
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (waveform != null && isAnalyzing) {
                            processWaveform(waveform)
                        }
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {} // We don't need frequency data, just amplitude
                }, Visualizer.getMaxCaptureRate() / 2, true, false)

                enabled = true
            }
            isAnalyzing = true
            Log.d("AudioAnalyzer", "Attached to System Audio Session 0")

        } catch (e: Exception) {
            Log.e("AudioAnalyzer", "Failed to attach to Session 0. OS might block it: ${e.message}")
        }
    }

    private fun processWaveform(waveform: ByteArray) {
        CoroutineScope(Dispatchers.Default).launch {
            var sum = 0.0
            
            // Visualizer bytes are unsigned 8-bit PCM offset by 128
            // A value of 128 means silence. 0 or 255 means max amplitude.
            for (byte in waveform) {
                val amplitude = byte.toInt() - 128 
                sum += (amplitude * amplitude)
            }
            
            val rms = sqrt(sum / waveform.size)

            // Convert to a rough synthetic dB value
            val syntheticDb = if (rms > 0) 20 * log10(rms) else 0.0

            val isTooLoud = syntheticDb > loudThreshold
            val isTooQuiet = syntheticDb > 5.0 && syntheticDb < quietThreshold

            if (isTooLoud || isTooQuiet) {
                withContext(Dispatchers.Main) {
                    volumeController.adjustVolume(isTooLoud, isTooQuiet)
                }
                delay(1500) // Cooldown to prevent volume jumping
            }
        }
    }

    fun stopAnalysis() {
        isAnalyzing = false
        visualizer?.enabled = false
        visualizer?.release()
        visualizer = null
    }
}
