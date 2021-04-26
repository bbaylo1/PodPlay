package com.brandonbaylosis.repository

import com.brandonbaylosis.service.ItunesService
import com.brandonbaylosis.service.PodcastResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 1 You define the primary constructor for ItunesRepo to require an existing instance
// of the ItunesService interface.
class ItunesRepo(private val itunesService: ItunesService) {
    // 2 Defines single parameter as a List of iTunesPodcast objects
    fun searchByTerm(term: String, callBack: (List<PodcastResponse.ItunesPodcast>?) -> Unit) {
        // 3 returns a Retrofit Call object
        val podcastCall = itunesService.searchPodcastByTerm(term)
        // 4 Invoke enqueue on the Call object, and it runs in the background to retrieve
        // the response from the web service.
        podcastCall.enqueue(object : Callback<PodcastResponse> {
            // 5 onFailure() is called if anything goes wrong with the call such as a network
            //error or an invalid URL
            override fun onFailure(call: Call<PodcastResponse>?,
                                   t: Throwable?) {
                // 6 If there's an error, call callBack() with a null value.
                callBack(null)
            }
            // 7 Call onResponse() if call succeeds
            override fun onResponse(
                call: Call<PodcastResponse>?,
                response: Response<PodcastResponse>?) {
                // 8 Retrieve the populated PodcastResponse model with a call to response.body().
                val body = response?.body()
                // 9 Call callBack() with the results object from the PodcastResponse model
                callBack(body?.results)
            }
        })
    }
}