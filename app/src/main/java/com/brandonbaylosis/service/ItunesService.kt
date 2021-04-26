package com.brandonbaylosis.service

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesService {
    // 1 Retrofit annotation
    @GET("/search?media=podcast")
    // 2 method takes a single parameter that has a @Query annotation Retrofit
    // that this parameter should be added as a query term in the path defined
    // by the @GET annotation.
    fun searchPodcastByTerm(@Query("term") term: String):
            Call<PodcastResponse>
    // 3 Defines a companion object in the ItunesService interface.
    companion object {
        // 4 Instance property of the companion object holds the only application-wide
        // instance of the ItunesService. Allows instance property to
        // return a Singleton object
        val instance: ItunesService by lazy {
            // 5 First part of lazy lambda method. Creates a retrofit builder object
            val retrofit = Retrofit.Builder()
                .baseUrl("https://itunes.apple.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            // 6 Creates ItunesService instance
            retrofit.create<ItunesService>(ItunesService::class.java)
        }
    }
}