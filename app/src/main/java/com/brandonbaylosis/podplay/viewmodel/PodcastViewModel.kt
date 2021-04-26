package com.brandonbaylosis.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.brandonbaylosis.podplay.model.Episode
import com.brandonbaylosis.podplay.model.Podcast
import com.brandonbaylosis.repository.PodcastRepo
import java.util.*

class PodcastViewModel(application: Application) :
    AndroidViewModel(application) {
    var podcastRepo: PodcastRepo? = null
    var activePodcastViewData: PodcastViewData? = null
    data class PodcastViewData(
        var subscribed: Boolean = false,
        var feedTitle: String? = "",
        var feedUrl: String? = "",
        var feedDesc: String? = "",
        var imageUrl: String? = "",
        var episodes: List<EpisodeViewData>
    )
    data class EpisodeViewData (
        var guid: String? = "",
        var title: String? = "",
        var description: String? = "",
        var mediaUrl: String? = "",
        var releaseDate: Date? = null,
        var duration: String? = ""
    )


    private fun episodesToEpisodesView(episodes: List<Episode>):
            List<EpisodeViewData> {
        return episodes.map {
            EpisodeViewData(it.guid, it.title, it.description,
                it.mediaUrl, it.releaseDate, it.duration)
        }
    }

    // 1 Method takes a PodcastSummaryViewData object and a callback method
    fun getPodcast(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData,
                   callback: (PodcastViewData?) -> Unit) {
        // 2 Local variables are assigned to podcastRepo and podcastSummaryViewData.feedUrl.
        // If either one is null, the method returns early
        val repo = podcastRepo ?: return
        val feedUrl = podcastSummaryViewData.feedUrl ?: return
        // 3 Call getPodcast() from the podcast repo with the feed URL.
        repo.getPodcast(feedUrl) {
            // 4 Check the podcast detail object to make sure it’s not null.
            it?.let {
                // 5 Set the podcast title to the podcast summary name
                it.feedTitle = podcastSummaryViewData.name ?: ""
                // 6 Set the podcast title to the podcast summary name
                it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
                // 7 Convert the Podcast object to a PodcastViewData object and assign it to
                //activePodcastViewData.
                activePodcastViewData = podcastToPodcastView(it)
                // 8  Call the callback method and pass the podcast view data
                callback(activePodcastViewData)
            }
        }
    }

    private fun podcastToPodcastView(podcast: Podcast):
            PodcastViewData {
        return PodcastViewData(
            false,
            podcast.feedTitle,
            podcast.feedUrl,
            podcast.feedDesc,
            podcast.imageUrl,
            episodesToEpisodesView(podcast.episodes)
        )
    }
}