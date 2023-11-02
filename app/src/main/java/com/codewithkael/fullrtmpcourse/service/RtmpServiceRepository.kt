package com.codewithkael.fullrtmpcourse.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RtmpServiceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun startService(url:String) {
        Thread {
            val intent = Intent(context, RtmpService::class.java)
            intent.action = RtmpServiceActions.START_SERVICE.name
            intent.putExtra("url", url)
            startServiceIntent(intent)
        }.start()
    }

    fun switchCamera() {
        val intent = Intent(context, RtmpService::class.java)
        intent.action = RtmpServiceActions.SWITCH_CAMERA.name
        startServiceIntent(intent)
    }

    fun stopService() {
        val intent = Intent(context, RtmpService::class.java)
        intent.action = RtmpServiceActions.STOP_SERVICE.name
        startServiceIntent(intent)
    }

    private fun startServiceIntent(intent: Intent) {
        context.startForegroundService(intent)
    }

}