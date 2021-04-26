package com.brandonbaylosis.repository

import com.brandonbaylosis.podplay.model.Episode
import com.brandonbaylosis.podplay.model.Podcast
import com.brandonbaylosis.podplay.util.DateUtils
import com.brandonbaylosis.service.FeedService
import com.brandonbaylosis.service.RssFeedResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastRepo(private var feedService: FeedService) {
    fun getPodcast(feedUrl: String, callback: (Podcast?) -> Unit) {
        feedService.getFeed(feedUrl) { feedResponse ->
            var podcast: Podcast? = null
            // If feedResponse is null, pass null to the callBack method. If feedResponse is valid,
            // then convert it to a Podcast object and pass to the callback method
            if (feedResponse != null) {
                podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
            }
            GlobalScope.launch(Dispatchers.Main) {
                callback(podcast)
            }
        }
    }

    // Uses the map method to convert a list of EpisodeResponse objects into a list of
    // Episode objects. The pubDate string is converted to a Date object using the new
    // xmlDateToDate method.
    private fun rssItemsToEpisodes(episodeResponses:
                                   List<RssFeedResponse.EpisodeResponse>): List<Episode> {
        return episodeResponses.map {
            Episode(
                    it.guid ?: "",
                    it.title ?: "",
                    it.description ?: "",
                    it.url ?: "",
                    it.type ?: "",
                    DateUtils.xmlDateToDate(it.pubDate),
                    it.duration ?: ""
            )
        }
    }

    private fun rssResponseToPodcast(feedUrl: String, imageUrl:
    String, rssResponse: RssFeedResponse): Podcast? {
        // 1 Assign list of episodes to items provided it’s not null; otherwise, the
        //method returns null
        val items = rssResponse.episodes ?: return null
        // 2 If the description is empty, the description property is set to the response
        // summary; otherwise, it’s set to the response description
        val description = if (rssResponse.description == "")
            rssResponse.summary else rssResponse.description
        // 3 Create a new Podcast object using the response data and then return it to
        // the caller.
        return Podcast(feedUrl, rssResponse.title, description,
                imageUrl, rssResponse.lastUpdated,
                episodes = rssItemsToEpisodes(items))
    }

}