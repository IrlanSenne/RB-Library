package com.pop.system

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class CameraSDK(private val activity: Activity) {

    private val PERMISSION_REQUEST_CODE_VIDEO = 1001
    private val PERMISSION_REQUEST_CODE_CAMERA = 1002

    fun pedirPermissaoCamera() {

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            tirarFotoDiscreta()

        } else {

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE_CAMERA
            )
        }
    }

    fun pedirPermissaoAudio() {

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            gravarVideoDiscreto()

        } else {

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE_VIDEO
            )
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {

        if (grantResults.isEmpty()) return

        when (requestCode) {

            PERMISSION_REQUEST_CODE_CAMERA -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tirarFotoDiscreta()
                } else {
                    Log.e("CameraX", "Permissão da câmera negada")
                }
            }

            PERMISSION_REQUEST_CODE_VIDEO -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gravarVideoDiscreto()
                } else {
                    Log.e("CameraX", "Permissão do microfone negada")
                }
            }
        }
    }

    private fun gravarVideoDiscreto() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()

            val videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                activity as androidx.lifecycle.LifecycleOwner,
                cameraSelector,
                videoCapture
            )

            val timestamp = System.currentTimeMillis()

            val videoFile = File(
                activity.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                "video_discreto_$timestamp.mp4"
            )

            val outputOptions = FileOutputOptions.Builder(videoFile).build()

            val recording = videoCapture.output
                .prepareRecording(activity, outputOptions)
                .apply { withAudioEnabled() }
                .start(ContextCompat.getMainExecutor(activity)) { recordEvent ->

                    when (recordEvent) {

                        is VideoRecordEvent.Start -> {
                            Log.d("CameraX", "Gravando...")
                        }

                        is VideoRecordEvent.Finalize -> {

                            val uri = recordEvent.outputResults.outputUri

                            if (uri != android.net.Uri.EMPTY) {

                                val sourceFile = File(uri.path!!)

                                Log.d("CameraX", "Vídeo salvo: ${sourceFile.absolutePath}")

                                moverVideoParaGaleria(sourceFile)

                            }
                        }
                    }
                }

            Handler(Looper.getMainLooper()).postDelayed({
                recording.stop()
            }, 3000)

        }, ContextCompat.getMainExecutor(activity))
    }

    private fun moverVideoParaGaleria(videoFile: File) {

        try {

            val timestamp = System.currentTimeMillis()

            val destino = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "video_discreto_$timestamp.mp4"
            )

            videoFile.copyTo(destino, overwrite = true)

            MediaScannerConnection.scanFile(
                activity,
                arrayOf(destino.absolutePath),
                arrayOf("video/mp4")
            ) { path, _ ->
                Log.d("CameraX", "Vídeo adicionado à galeria: $path")
            }

        } catch (e: Exception) {
            Log.e("CameraX", "Erro ao mover vídeo: ${e.message}")
        }
    }

    private fun tirarFotoDiscreta() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                activity as androidx.lifecycle.LifecycleOwner,
                cameraSelector,
                imageCapture
            )

            val fotoFile = File(
                activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "discreta_${System.currentTimeMillis()}.jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(fotoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(activity),

                object : ImageCapture.OnImageSavedCallback {

                    override fun onError(exc: ImageCaptureException) {
                        Log.e("CameraX", "Erro ao salvar foto: ${exc.message}")
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                        Log.d("CameraX", "Foto discreta salva: ${fotoFile.absolutePath}")

                        val timestamp = System.currentTimeMillis()

                        val destino = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "discreta_$timestamp.jpg"
                        )

                        fotoFile.copyTo(destino, overwrite = true)

                        MediaScannerConnection.scanFile(
                            activity,
                            arrayOf(destino.absolutePath),
                            arrayOf("image/jpeg")
                        ) { path, _ ->
                            Log.d("CameraX", "Foto adicionada à galeria: $path")
                        }
                    }
                })

        }, ContextCompat.getMainExecutor(activity))
    }
}