package io.benreynolds.giffit.cloudvisionapi.models.requests

/**
 * Type of Google Cloud Vision API detection to perform, and the maximum number of results to return
 * for that type.
 *
 * @property Feature Feature type.
 * @property maxResults Maximum number of results of this type.
 * @property model Model to use for the feature. Supported values: "builtin/stable" (the default if
 * unset) and "builtin/latest".
 */
data class Feature(val type: String, val maxResults: Int, val model: String) {
    /**
     * Type of Google Cloud Vision API feature to be extracted.
     */
    companion object Type {
        /**
         * Unspecified feature type.
         */
        const val LABEL_DETECTION = "LABEL_DETECTION"
    }
}
