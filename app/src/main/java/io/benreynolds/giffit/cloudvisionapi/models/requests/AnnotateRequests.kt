package io.benreynolds.giffit.cloudvisionapi.models.requests

/**
 * Request body for the Google Vision API annotate method.
 *
 * @property requests Individual image annotation requests for this batch.
 */
data class AnnotateRequests(val requests: List<AnnotateImageRequest>)
