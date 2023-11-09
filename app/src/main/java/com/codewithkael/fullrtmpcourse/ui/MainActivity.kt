package com.codewithkael.fullrtmpcourse.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codewithkael.fullrtmpcourse.databinding.ActivityMainBinding
import com.codewithkael.fullrtmpcourse.service.RtmpService
import com.codewithkael.fullrtmpcourse.service.RtmpServiceRepository
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), RtmpService.Listener {

    private lateinit var views: ActivityMainBinding
    @Inject
    lateinit var repository: RtmpServiceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = ActivityMainBinding.inflate(layoutInflater)
        setContentView(views.root)
        setupViews()
    }

    private fun setupViews() {
        RtmpService.listener = this@MainActivity
        if (RtmpService.isStreamOn) {
            streamStarted()
            views.urlEt.setText(RtmpService.currentUrl)
        }
        views.apply {
            startBtn.setOnClickListener {
                getPermissions {
                    if (it) {
                        if (urlEt.text.toString().isNotEmpty()) {
                            repository.startService(views.urlEt.text.toString())
                            startBtn.isEnabled = false
                            statusTv.text = "Status: Connecting"
                        } else {
                            Toast.makeText(this@MainActivity, "Enter url", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Permissions not granted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            stopBtn.setOnClickListener {
                repository.stopService()
                streamStopped()
            }

            switchBtn.setOnClickListener {
                repository.switchCamera()
            }
        }
    }

    private fun streamStarted() {
        views.apply {
            runOnUiThread {
                urlEt.isEnabled = false
                frameLayout.addView(RtmpService.surfaceView)
                statusTv.text = "Status: Connected"
                startBtn.isEnabled = false
                stopBtn.isEnabled = true
                switchBtn.isEnabled = true
            }
        }
    }

    private fun streamStopped() {
        runOnUiThread {
            views.apply {
                urlEt.isEnabled = true
                statusTv.text = "Status: Not Connected"
                startBtn.isEnabled = true
                stopBtn.isEnabled = false
                switchBtn.isEnabled = false
                frameLayout.removeView(RtmpService.surfaceView)
            }
        }
    }

    override fun onDestroy() {
        views.frameLayout.removeView(RtmpService.surfaceView)
        super.onDestroy()
    }

    private fun getPermissions(granted: (Boolean) -> Unit) {
        PermissionX.init(this)
            .permissions(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CAMERA
            )
            .request { allGranted, _, _ ->
                granted(allGranted)
            }
    }

    override fun onStreamStarted() {
        streamStarted()
    }

    override fun onStreamClosed() {
        streamStopped()
    }
}