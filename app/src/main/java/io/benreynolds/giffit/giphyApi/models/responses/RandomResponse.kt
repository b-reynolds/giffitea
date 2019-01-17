package io.benreynolds.giffit.giphyApi.models.responses

import com.google.gson.annotations.SerializedName

/**
 * Response to a random request.
 *
 * @property gifObject contains URLs for GIFs in many different formats and sizes.
 */
data class RandomResponse(
  @SerializedName("data")
  val gifObject: GifObject? = null
)