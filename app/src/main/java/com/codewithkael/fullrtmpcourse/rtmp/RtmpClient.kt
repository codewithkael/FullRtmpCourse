package com.codewithkael.fullrtmpcourse.rtmp

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.util.Log
import com.haishinkit.event.Event
import com.haishinkit.event.IEventListener
import com.haishinkit.media.Camera2Source
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpStream
import com.haishinkit.view.HkSurfaceView
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RtmpClient (
    private val surfaceView:HkSurfaceView,
    private val listener : Listener
) : IEventListener {

    private var connection: RtmpConnection = RtmpConnection()
    private var stream: RtmpStream = RtmpStream(connection)
    private var videoSource: Camera2Source = Camera2Source(surfaceView.context).apply {
        open(CameraCharacteristics.LENS_FACING_BACK)
    }

    fun init(){
        stream.attachVideo(videoSource)
        connection.addEventListener(Event.RTMP_STATUS, this@RtmpClient)
        surfaceView.attachStream(stream)

        //you can set custom configs here using init function
        stream.videoSetting.width = 480 // The width  of video output.
        stream.videoSetting.height = 720 // The height  of video output.
        stream.videoSetting.bitRate = 1_000_000 // The bitRate of video output.
        stream.videoSetting.frameRate = 15

    }

    fun start(url:String){
        connection.connect(url)
        stream.publish("My Awesome Stream")
    }

    fun stop() {
        try {
            videoSource.close()
            stream.close()
            connection.close()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun switchCamera(){
        videoSource.switchCamera()
    }


    override fun handleEvent(event: Event) {
        if (event.data.toString().contains("code=NetConnection.Connect.Success")
            && event.type == "rtmpStatus"
        ){
            listener.onStreamConnected()
        }
        if (event.data.toString().contains("code=NetConnection.Connect.Closed")
            && event.type == "rtmpStatus"
        ){
            listener.onStreamClosed()
        }
    }


    interface Listener {
        fun onStreamConnected()
        fun onStreamClosed()
    }

}