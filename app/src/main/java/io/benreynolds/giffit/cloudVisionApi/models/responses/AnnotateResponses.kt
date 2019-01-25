package io.benreynolds.giffit.cloudVisionApi.models.responses

/**
 * Response to a batch image annotation request.
 *
 * @property responses Individual responses to image annotation requests within the batch.
 */
data class AnnotateResponses(var responses: List<AnnotateImageResponse>? = null)
