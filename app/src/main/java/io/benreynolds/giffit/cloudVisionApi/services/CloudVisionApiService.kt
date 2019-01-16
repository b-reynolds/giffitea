package io.benreynolds.giffit.cloudVisionApi.services

import io.benreynolds.giffit.cloudVisionApi.models.requests.AnnotateRequests
import io.benreynolds.giffit.cloudVisionApi.models.responses.AnnotateResponses
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Defines methods that interact with the Google's Cloud Vision API.
 */
interface CloudVisionApiService {
  /**
   * Run image detection and annotation for a batch of images.
   *
   * @param annotateRequests Individual image annotation annotateRequests for this batch.
   */
  @POST("/v1/images:annotate")
  fun annotate(
    @Query("key") apiKey: String,
    @Body annotateRequests: AnnotateRequests
  ): Call<AnnotateResponses>
}