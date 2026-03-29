package com.example.volumestabilizer

import android.content.Context
import android.media.AudioManager
import android.util.Log

class VolumeController(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    
    // Limits to prevent the app from muting or blowing out speakers
    private val minAllowedVolume = (maxVolume * 0.1).toInt() 
    private val maxAllowedVolume = (maxVolume * 0.8).toInt()

    fun adjustVolume(isTooLoud: Boolean, isTooQuiet: Boolean) {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        if (isTooLoud && currentVolume > minAllowedVolume) {
            Log.d("VolumeController", "Audio too loud! Lowering volume.")
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                currentVolume - 1,
                AudioManager.FLAG_SHOW_UI // Shows the native TV volume UI
            )
        } else if (isTooQuiet && currentVolume < maxAllowedVolume) {
            Log.d("VolumeController", "Audio too quiet. Raising volume.")
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                currentVolume + 1,
                AudioManager.FLAG_SHOW_UI
            )
        }
    }
}
