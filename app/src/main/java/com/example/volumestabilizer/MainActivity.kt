package com.example.volumestabilizer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
        }

        val toggleButton = Button(this).apply {
            text = "Start Stabilizer"
            isFocusable = true
            isFocusableInTouchMode = true
            setOnFocusChangeListener { view, hasFocus ->
                view.setBackgroundColor(if (hasFocus) 0xFF00FF00.toInt() else 0xFFCCCCCC.toInt())
            }
            setOnClickListener {
                if (checkPermissions()) {
                    toggleService(this)
                }
            }
        }

        layout.addView(toggleButton)
        setContentView(layout)
    }

    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, 
                arrayOf(Manifest.permission.RECORD_AUDIO), 
                100
            )
            return false
        }
        return true
    }

    private var isServiceRunning = false

    private fun toggleService(button: Button) {
        val intent = Intent(this, StabilizerService::class.java)
        if (isServiceRunning) {
            stopService(intent)
            button.text = "Start Stabilizer"
        } else {
            ContextCompat.startForegroundService(this, intent)
            button.text = "Stop Stabilizer"
        }
        isServiceRunning = !isServiceRunning
    }
}
