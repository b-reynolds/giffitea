package io.benreynolds.giffit

import java.util.*

/**
 * Polls each element within the [] the given [action] on each element within the [Deque].
 */
fun <T> Queue<T>.pollEach(action: (T) -> Unit) {
  for (i in 0 until size) {
    action(poll())
  }
}