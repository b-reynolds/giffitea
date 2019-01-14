package io.benreynolds.giffit

import io.benreynolds.giffit.extensions.pollEach
import java.util.LinkedList
import java.util.Queue

/**
 * [CallbackManager] stores callbacks in a queue by key and provides methods such as [invokeAll] and
 * [clearAll] that allow you to easily manage a queue of callbacks.
 */
class CallbackManager<T> : HashMap<T, Queue<() -> Unit>>() {
  /**
   * Adds the [callback] to the back of the queue with the specified [key].
   */
  fun add(key: T, callback: () -> Unit) {
    if (!containsKey(key)) {
      put(key, LinkedList())
    }

    get(key)?.add(callback)
  }

  /**
   * Invokes all of the callbacks within the queue with the specified [key].
   */
  fun invokeAll(key: T) = get(key)?.pollEach { it.invoke() }

  /**
   * Removes all of the callbacks from the queue with the specified [key].
   */
  fun clearAll(key: T) = get(key)?.clear()
}