package io.benreynolds.giffit.cloudVisionApi.models.requests

/**
 * Client image to perform Google Cloud Vision API tasks over.
 *
 * @property content Image content, represented as a base64-encoded string.
 */
data class Image(val content: String)