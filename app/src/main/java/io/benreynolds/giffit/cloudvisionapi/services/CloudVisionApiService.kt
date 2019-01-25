package io.benreynolds.giffit.cloudvisionapi.services

import io.benreynolds.giffit.cloudvisionapi.models.requests.AnnotateImageRequest
import io.benreynolds.giffit.cloudvisionapi.models.requests.AnnotateRequests
import io.benreynolds.giffit.cloudvisionapi.models.requests.Feature
import io.benreynolds.giffit.cloudvisionapi.models.requests.Image
import io.benreynolds.giffit.cloudvisionapi.models.responses.AnnotateResponses
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

fun CloudVisionApiService.annotate(apiKey: String, image: String): Call<AnnotateResponses> {
    return annotate(
        apiKey,
        AnnotateRequests(
            listOf(
                AnnotateImageRequest(
                    Image(image),
                    listOf(Feature(Feature.LABEL_DETECTION, 1, "builtin/stable"))
                )
            )
        )
    )
}
