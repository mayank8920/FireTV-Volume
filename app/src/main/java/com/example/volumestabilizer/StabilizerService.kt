package com.example.volumestabilizer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class StabilizerService : Service() {
    private lateinit var audioAnalyzer: AudioAnalyzer
    private lateinit var volumeController: VolumeController

    override fun onCreate() {
        super.onCreate()
        volumeController = VolumeController(this)
        audioAnalyzer = AudioAnalyzer(volumeController)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "STABILIZER_CHANNEL")
            .setContentTitle("Smart Volume Active")
            .setContentText("Listening to system audio via Visualizer...")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .build()

        startForeground(1, notification)
        audioAnalyzer.startAnalysis()
        
        return START_STICKY 
    }

    override fun onDestroy() {
        audioAnalyzer.stopAnalysis()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "STABILIZER_CHANNEL",
                "Volume Stabilizer",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
