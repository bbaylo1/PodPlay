package com.brandonbaylosis.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.brandonbaylosis.podplay.util.DateUtils
import com.brandonbaylosis.repository.ItunesRepo
import com.brandonbaylosis.service.PodcastResponse

class SearchViewModel(application: Application) :
    AndroidViewModel(application) {
    var iTunesRepo: ItunesRepo? = null

    // Defines a data class within the view model that has only the data that’s
    //necessary for the View, and that has default empty string values:
    data class PodcastSummaryViewData(
        var name: String? = "",
        var lastUpdated: String? = "",
        var imageUrl: String? = "",
        var feedUrl: String? = "")

    // Helper method to convert from the raw model data to the view data
    private fun itunesPodcastToPodcastSummaryView(
        itunesPodcast: PodcastResponse.ItunesPodcast): PodcastSummaryViewData {
        return PodcastSummaryViewData(
            itunesPodcast.collectionCensoredName,
            DateUtils.jsonDateToShortDate(itunesPodcast.releaseDate),
            itunesPodcast.artworkUrl30,
            itunesPodcast.feedUrl)
    }

    // 1
    fun searchPodcasts(term: String, callback: (List<PodcastSummaryViewData>) -> Unit) {
        // 2
        iTunesRepo?.searchByTerm(term) { results ->
            if (results == null) {
                // 3
                callback(emptyList())
            } else {
                // 4
                val searchViews = results.map { podcast ->
                    itunesPodcastToPodcastSummaryView(podcast)
                }
                // 5
                callback(searchViews)
            }
        }
    }
    }