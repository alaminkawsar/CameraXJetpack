package com.example.myapplication.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class CameraViewModel(): ViewModel() {

    companion object {
        val CAMERAX_PERMISSION = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    private var recording: Recording? = null

    val bitmaps = _bitmaps.asStateFlow()

    fun requestAllPermission(context: Context) {
        if (!hasRequiredPermissions(context)) {
            ActivityCompat.requestPermissions(
                context as Activity, CAMERAX_PERMISSION,0
            )
        }
    }
    private fun hasRequiredPermissions(context: Context): Boolean {
        return CAMERAX_PERMISSION.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value += bitmap
    }
    fun recordVideo(
        controller: LifecycleCameraController,
        context: Context
    ) {
        if (recording != null) {
            recording?.stop()
            recording = null
            return;
        }
        val outputFile = File(context.filesDir, "my-recording.mp4")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        recording = controller.startRecording(
            FileOutputOptions.Builder(outputFile).build(),
            AudioConfig.create(true),
            ContextCompat.getMainExecutor(context)
        ) {event->
            when(event) {
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        recording?.close()
                        recording = null

                        Toast.makeText(
                            context,
                            "Video capture failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Video capture Succeeded",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            }
        }
    }
}