package com.codewithkael.fullrtmpcourse.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codewithkael.fullrtmpcourse.service.RtmpServiceRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CloseActivity : AppCompatActivity() {

    @Inject lateinit var repository: RtmpServiceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository.stopService()
        finishAffinity()
    }
}