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

val giphyApi: GiphyApiService by lazy {
  Retrofit.Builder()
    .baseUrl("https://api.giphy.com")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(GiphyApiService::class.java)
}

val cloudVisionApi: CloudVisionApiService by lazy {
  Retrofit.Builder()
    .baseUrl("https://vision.googleapis.com")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(CloudVisionApiService::class.java)
}

class ImageSelectionViewModel : ViewModel() {
  fun onImageSelected(imageFile: File, onGifRetrieved: (String) -> Unit) {
    requestImageIdentity(
      imageFile,
      onSuccess = { identity ->
        requestRandomGif(
          identity,
          onSuccess = { gifUrl ->
            onGifRetrieved(gifUrl)
          },
          onFailure = {}
        )
      },
      onFailure = {}
    )
  }

  private fun requestRandomGif(
    searchQuery: String,
    onSuccess: ((String) -> Unit)? = null,
    onFailure: (() -> Unit)? = null
  ) {
    giphyApi.random(BuildConfig.GIPHY_API_KEY, searchQuery)
      .enqueueKt(
        onSuccess = { response ->
          response.body()?.gifObject?.images?.original?.url?.let { gifUrl ->
            onSuccess?.invoke(gifUrl)
          } ?: onFailure?.invoke()
        },
        onFailure = { onFailure?.invoke() }
      )
  }

  private fun requestImageIdentity(
    imageFile: File,
    onSuccess: ((String) -> Unit)? = null,
    onFailure: (() -> Unit)? = null
  ) {
    if (!imageFile.isFile && !imageFile.canRead()) {
      onFailure?.invoke()
      return
    }

    val encodedImage = imageFile.toBitmap()?.toBase64EncodedString()
    if (encodedImage == null) {
      onFailure?.invoke()
      return
    }

    cloudVisionApi
      .annotate(BuildConfig.CLOUD_VISION_API_KEY, encodedImage)
      .enqueueKt(
        onSuccess = { response ->
          response.body()?.responses?.firstOrNull()?.labelAnnotations?.firstOrNull()
            ?.description?.let { identity ->
            onSuccess?.invoke(identity)
          }
        },
        onFailure = { onFailure?.invoke() }
      )
  }
}