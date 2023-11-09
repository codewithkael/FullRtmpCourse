package com.codewithkael.fullrtmpcourse.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.codewithkael.fullrtmpcourse.R
import com.codewithkael.fullrtmpcourse.rtmp.RtmpClient
import com.haishinkit.view.HkSurfaceView

class RtmpService : LifecycleService(), RtmpClient.Listener {

    companion object {
        var surfaceView: HkSurfaceView? = null
        var isServiceRunning = false
        var isStreamOn = false
        var listener: Listener? = null
        var currentUrl = ""
    }

    //notification section
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var rtmpClient: RtmpClient? = null

    override fun onCreate() {
        super.onCreate()
        setupNotification()
        surfaceView = HkSurfaceView(this)
        rtmpClient = RtmpClient(this, surfaceView!!, this@RtmpService)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.let { incomingIntent ->
            when (incomingIntent.action) {
                RtmpServiceActions.START_SERVICE.name -> handleStartService(intent)
                RtmpServiceActions.STOP_SERVICE.name -> handleStopService()
                RtmpServiceActions.SWITCH_CAMERA.name -> handleSwitchCamera()
                else -> Unit
            }
        }

        return START_STICKY
    }


    private fun handleStartService(intent: Intent) {

        val url = intent.getStringExtra("url")
        url?.let {
            currentUrl = it
            rtmpClient?.start(it)
            startServiceWithNotification()
        }
    }


    private fun handleStopService() {
        startServiceWithNotification()
        isServiceRunning = false
        isStreamOn = false
        rtmpClient?.stop()
        surfaceView?.dispose()
        surfaceView = null
        stopSelf()
        notificationManager.cancelAll()

    }

    private fun handleSwitchCamera() {
        rtmpClient?.switchCamera()
    }


    private fun setupNotification() {
        notificationManager = getSystemService(
            NotificationManager::class.java
        )
        val notificationChannel = NotificationChannel(
            "rtmpChannel", "foreground", NotificationManager.IMPORTANCE_HIGH
        )

        val intent = Intent(this, RtmpBroadcastReceiver::class.java).apply {
            action = "ACTION_EXIT"
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        notificationManager.createNotificationChannel(notificationChannel)
        notificationBuilder = NotificationCompat.Builder(
            this, "rtmpChannel"
        ).setSmallIcon(R.mipmap.ic_launcher)
            .addAction(R.drawable.ic_end_call, "Exit", pendingIntent)
    }

    private fun startServiceWithNotification() {
        startForeground(1, notificationBuilder.build())
    }


    interface Listener {
        fun onStreamStarted()
        fun onStreamClosed()
    }

    override fun onStreamConnected() {
        isStreamOn = true
        listener?.onStreamStarted()
    }

    override fun onStreamClosed() {
        listener?.onStreamClosed()
    }
}