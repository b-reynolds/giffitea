package io.benreynolds.giffit.extensions

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

fun Bitmap.toBase64EncodedString(quality: Int = 50): String? {
  val byteArrayOutputStream = ByteArrayOutputStream()
  if (!compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)) {
    return null
  }
  return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)
}