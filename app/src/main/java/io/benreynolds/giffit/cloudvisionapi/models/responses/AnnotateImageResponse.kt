package io.benreynolds.giffit.cloudvisionapi.models.responses

import io.benreynolds.giffit.cloudvisionapi.models.requests.AnnotateImageRequest

/**
 * AnnotateResponses to an [AnnotateImageRequest].
 *
 * @property labelAnnotations If present, label detection has completed successfully.
 */
data class AnnotateImageResponse(val labelAnnotations: List<EntityAnnotation>? = null)
