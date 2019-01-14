package io.benreynolds.giffit.extensions

import java.util.*

/**
 * Polls each element within the [Queue] passing them to the specified [action].
 */
fun <T> Queue<T>.pollEach(action: (T) -> Unit) {
  for (i in 0 until size) {
    action(poll())
  }
}