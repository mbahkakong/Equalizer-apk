package com.example.universalequalizer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class EQService : Service() {

    companion object {
        var equalizer: Equalizer? = null
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        try {
            // AudioSessionId 0 = Menargetkan seluruh Output Audio Sistem HP
            equalizer = Equalizer(0, 0).apply {
                enabled = true
            }
            isRunning = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val band = intent?.getIntExtra("BAND", -1) ?: -1
        val level = intent?.getIntExtra("LEVEL", 0) ?: 0

        if (band != -1 && equalizer != null) {
            try {
                equalizer?.setBandLevel(band.toShort(), level.toShort())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "eq_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Equalizer Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Universal Equalizer Aktif")
            .setContentText("Memproses suara sistem di background")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        equalizer?.enabled = false
        equalizer?.release()
        equalizer = null
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
