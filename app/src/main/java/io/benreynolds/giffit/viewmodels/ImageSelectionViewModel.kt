package io.benreynolds.giffit.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.benreynolds.giffit.BuildConfig
import io.benreynolds.giffit.cloudvisionapi.services.CloudVisionApiService
import io.benreynolds.giffit.cloudvisionapi.services.annotate
import io.benreynolds.giffit.extensions.enqueueKt
import io.benreynolds.giffit.extensions.toBase64EncodedString
import io.benreynolds.giffit.extensions.toBitmap
import io.benreynolds.giffit.giphyapi.services.GiphyApiService
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
    val loading: MutableLiveData<GiffiteaLoadingState> = MutableLiveData()

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
                    onSuccess = { url ->
                        onSuccess?.invoke(url)
                        loading.value = GiffiteaLoadingState.DONE
                    },
                    onFailure = { error ->
                        onFailure?.invoke(error)
                        loading.value = GiffiteaLoadingState.DONE
                    }
                )
            },
            onFailure = { error ->
                onFailure?.invoke(error)
                loading.value = GiffiteaLoadingState.DONE
            }
        )
    }

    private fun requestRandomGif(
        searchQuery: String,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((GiffiteaError) -> Unit)? = null
    ) {
        loading.value = GiffiteaLoadingState.RETRIEVING_GIF
        giphyApi.random(BuildConfig.GIPHY_API_KEY, searchQuery)
            .enqueueKt(
                onSuccess = { response ->
                    val url = response.body()?.gifObject?.images?.original?.url
                    loading.value = GiffiteaLoadingState.DONE
                    if (url == null) {
                        onFailure?.invoke(GiffiteaError.INVALID_API_RESPONSE)
                    } else {
                        onSuccess?.invoke(url)
                    }
                },
                onFailure = {
                    loading.value = GiffiteaLoadingState.DONE
                    onFailure?.invoke(GiffiteaError.INVALID_API_RESPONSE)
                }
            )
    }

    private fun requestImageAnnotation(
        imageFile: File,
        onSuccess: ((String) -> Unit)? = null,
        onFailure: ((GiffiteaError) -> Unit)? = null
    ) {
        loading.value = GiffiteaLoadingState.IDENTIFYING_IMAGE
        if (!imageFile.isFile && !imageFile.canRead()) {
            loading.value = GiffiteaLoadingState.DONE
            onFailure?.invoke(GiffiteaError.INVALID_IMAGE_FILE)
            return
        }

        val encodedImage = imageFile.toBitmap()?.toBase64EncodedString()
        if (encodedImage == null) {
            loading.value = GiffiteaLoadingState.DONE
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

                    loading.value = GiffiteaLoadingState.DONE
                    if (annotation == null) {
                        onFailure?.invoke(GiffiteaError.INVALID_API_RESPONSE)
                    } else {
                        onSuccess?.invoke(annotation)
                    }
                },
                onFailure = {
                    loading.value = GiffiteaLoadingState.DONE
                    onFailure?.invoke(GiffiteaError.INVALID_API_RESPONSE)
                }
            )
    }
}

enum class GiffiteaError {
    INVALID_API_RESPONSE,
    INVALID_IMAGE_FILE
}

enum class GiffiteaLoadingState {
    IDENTIFYING_IMAGE,
    RETRIEVING_GIF,
    DONE
}
