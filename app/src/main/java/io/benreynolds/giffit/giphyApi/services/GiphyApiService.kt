package io.benreynolds.giffit.giphyApi.services

import io.benreynolds.giffit.giphyApi.models.responses.RandomResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Defines methods that interact with the Giphy API.
 */
interface GiphyApiService {
  /**
   * Returns a single random GIF, optionally limited to a specified tag.
   *
   * @property apiKey GIPHY API Key.
   * @property tag Filters results by specified tag.
   */
  @GET("/v1/gifs/random")
  fun random(
    @Query("api_key") apiKey: String,
    @Query("tag") tag: String
  ): Call<RandomResponse>
}