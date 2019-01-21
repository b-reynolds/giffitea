package io.benreynolds.giffit.viewModels

import androidx.lifecycle.ViewModel
import io.benreynolds.giffit.BuildConfig
import io.benreynolds.giffit.cloudVisionApi.services.CloudVisionApiService
import io.benreynolds.giffit.cloudVisionApi.services.annotate
import io.benreynolds.giffit.extensions.enqueueKt
import io.benreynolds.giffit.extensions.toBase64EncodedString
import io.benreynolds.giffit.extensions.toBitmap
import io.benreynolds.giffit.giphyApi.services.GiphyApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

private val giphyApi: GiphyApiService by lazy {
    Retrofit.Builder()
        .baseUrl("https://api.giphy.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GiphyApiService::class.java)
}

private val cloudVisionApi: CloudVisionApiService by lazy {
    Retrofit.Builder()
        .baseUrl("https://vision.googleapis.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CloudVisionApiService::class.java)
}

class ImageSelectionViewModel : ViewModel() {
    fun requestRandomGifForImage(
        imageFile: File,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((GiffiteaError) -> (Unit))? = null
    ) {
        requestImageAnnotation(
            imageFile,
            onSuccess = {
                requestRandomGif(
                    it,
                    onSuccess = { url -> onSuccess?.invoke(url) },
                    onFailure = { error -> onFailure?.invoke(error) })
            },
            onFailure = { error -> onFailure?.invoke(error) }
        )
    }

    private fun requestRandomGif(
        searchQuery: String,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((GiffiteaError) -> Unit)? = null
    ) {
        giphyApi.random(BuildConfig.GIPHY_API_KEY, searchQuery)
            .enqueueKt(
                onSuccess = { response ->
                    val url = response.body()?.gifObject?.images?.original?.url
                    if (url == null) {
                        onFailure?.invoke(GiffiteaError.INVALID_API_RESPONSE)
                    } else {
                        onSuccess?.invoke(url)
                    }
                },
                onFailure = { onFailure?.invoke(GiffiteaError.INVALID_API_RESPONSE) }
            )
    }

    private fun requestImageAnnotation(
        imageFile: File,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((GiffiteaError) -> Unit)? = null
    ) {
        if (!imageFile.isFile && !imageFile.canRead()) {
            onFailure?.invoke(GiffiteaError.INVALID_IMAGE_FILE)
            return
        }

        val encodedImage = imageFile.toBitmap()?.toBase64EncodedString()
        if (encodedImage == null) {
            onFailure?.invoke(GiffiteaError.INVALID_IMAGE_FILE)
            return
        }

        cloudVisionApi
            .annotate(BuildConfig.CLOUD_VISION_API_KEY, encodedImage)
            .enqueueKt(
                onSuccess = { response ->
                    val annotation = response
                        .body()
                        ?.responses
                        ?.firstOrNull()
                        ?.labelAnnotations
                        ?.firstOrNull()
                        ?.description

                    if (annotation == null) {
                        onFailure?.invoke(GiffiteaError.INVALID_API_RESPONSE)
                    } else {
                        onSuccess?.invoke(annotation)
                    }
                },
                onFailure = { onFailure?.invoke(GiffiteaError.INVALID_API_RESPONSE) }
            )
    }
}

enum class GiffiteaError {
    INVALID_API_RESPONSE,
    INVALID_IMAGE_FILE
}