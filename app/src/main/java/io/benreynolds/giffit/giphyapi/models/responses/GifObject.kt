package io.benreynolds.giffit.giphyapi.models.responses

/**
 * Contains a variety of information, including the [images] object, which contains URLs for GIFs in
 * many different formats and sizes.
 *
 * @property images An object containing data for various available formats and sizes of this GIF.
 */
data class GifObject(val images: Images? = null)
