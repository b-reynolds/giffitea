package io.benreynolds.giffit.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

fun File.toBitmap(): Bitmap? {
    return try {
        BitmapFactory.decodeFile(absolutePath)
    } catch (exception: IllegalArgumentException) {
        return null
    }
}
