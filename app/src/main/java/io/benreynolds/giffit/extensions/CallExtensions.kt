package io.benreynolds.giffit.extensions

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun <T> Call<T>.enqueueKt(onSuccess: (Response<T>) -> Unit, onFailure: (t: Throwable?) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>?, throwable: Throwable?) = onFailure(throwable)
        override fun onResponse(call: Call<T>?, response: Response<T>) = onSuccess(response)
    })
}
