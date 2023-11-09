package com.codewithkael.fullrtmpcourse.rtmp

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import com.haishinkit.event.Event
import com.haishinkit.event.IEventListener
import com.haishinkit.media.AudioRecordSource
import com.haishinkit.media.AudioSource
import com.haishinkit.media.Camera2Source
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpStream
import com.haishinkit.view.HkSurfaceView
import java.util.UUID
import javax.inject.Singleton

@Singleton
class RtmpClient(
    context: Context,
    surfaceView: HkSurfaceView,
    private val listener: Listener
) : IEventListener {

    private var connection: RtmpConnection = RtmpConnection()
    private var stream: RtmpStream = RtmpStream(connection)
    private val videoSource: Camera2Source by lazy {
        Camera2Source(context).apply {
            open(CameraCharacteristics.LENS_FACING_BACK)
        }
    }
    private val audioSource: AudioSource by lazy {
        AudioRecordSource(context)
    }


    init {
        stream.attachAudio(audioSource)
        stream.attachVideo(videoSource)
        connection.addEventListener(Event.RTMP_STATUS, this@RtmpClient)
        surfaceView.attachStream(stream)

    }

    fun start(url: String) {
        //you can set custom configs here using init function
        stream.audioSetting.bitRate = 32 * 1000

        stream.videoSetting.width = 640 // The width resoulution of video output.
        stream.videoSetting.height = 360 // The height resoulution of video output.
        stream.videoSetting.bitRate = 1000000 // The bitRate of video output.
        stream.videoSetting.IFrameInterval = 2 // The key-frmae interval
        connection.connect(url)
        stream.publish(UUID.randomUUID().toString())

    }

    fun stop() {
        try {
            videoSource.close()
            audioSource.tearDown()
            stream.close()
            connection.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun switchCamera() {
        videoSource.switchCamera()
    }


    override fun handleEvent(event: Event) {

        if (event.data.toString().contains("code=NetConnection.Connect.Success")
            && event.type == "rtmpStatus"
        ) {
            listener.onStreamConnected()
        }
        if (event.data.toString().contains("code=NetConnection.Connect.Closed")
            && event.type == "rtmpStatus"
        ) {
            listener.onStreamClosed()
        }
    }


    interface Listener {
        fun onStreamConnected()
        fun onStreamClosed()
    }
}