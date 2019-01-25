package io.benreynolds.giffit.cloudvisionapi.models.requests

/**
 * Request body to run image detection and annotation for a batch of images.
 *
 * @property image Image to be processed.
 * @property features Requested features.
 */
data class AnnotateImageRequest(val image: Image, val features: List<Feature>)
