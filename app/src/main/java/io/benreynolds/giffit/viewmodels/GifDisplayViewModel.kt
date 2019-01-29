package io.benreynolds.giffit.viewmodels

import android.os.Environment
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.io.IOException

class GifDisplayViewModel : ViewModel() {
    private val storageDirectory by lazy {
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Giffitea").apply {
            mkdirs()
        }
    }

    suspend fun downloadGif(
        gifUrl: String,
        onDownloaded: ((File) -> Unit)? = null,
        onDownloadFailed: ((GiffiteaError) -> Unit)? = null
    ) {
        val outputFile = File(storageDirectory, "/${System.currentTimeMillis()}.gif")
        withContext(Dispatchers.Default) {
            try {
                URL(gifUrl).openStream().use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (exception: IOException) {
                onDownloadFailed?.invoke(GiffiteaError.DOWNLOAD_FAILED)
            }
        }

        if (outputFile.isFile && outputFile.canRead()) {
            onDownloaded?.invoke(outputFile)
        } else {
            onDownloadFailed?.invoke(GiffiteaError.DOWNLOAD_FAILED)
        }
    }
}