package io.benreynolds.giffit.cloudVisionApi.models.responses

import io.benreynolds.giffit.cloudVisionApi.models.requests.AnnotateImageRequest

/**
 * AnnotateResponses to an [AnnotateImageRequest].
 *
 * @property labelAnnotations If present, label detection has completed successfully.
 */
data class AnnotateImageResponse(val labelAnnotations: List<EntityAnnotation>? = null)