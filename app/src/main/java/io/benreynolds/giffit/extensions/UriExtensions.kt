package io.benreynolds.giffit.extensions

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

/**
 * Returns a [File] that points to the specified [Uri]. If the file could not be found, returns
 * `null`.
 */
fun Uri.getFile(context: Context): File? {
  context
    .contentResolver
    ?.query(this, null, null, null, null)
    ?.use { cursor ->
      cursor.moveToFirst()
      val columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
      return if (columnIndex == -1) {
        null
      } else {
        File(cursor.getString(columnIndex))
      }
    } ?: return File(path)
}