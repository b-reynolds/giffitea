package io.benreynolds.giffit.giphyapi.models.responses

/**
 * Contains the URLs for GIFs in many different formats and sizes.
 *
 * @property original Object containing gifObject for various available formats and sizes of this
 * GIF.
 */
data class Images(var original: Original? = null)
