package com.brandonbaylosis.service

import com.brandonbaylosis.podplay.util.DateUtils
import okhttp3.*
import org.w3c.dom.Node
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory

class RssFeedService: FeedService {
    override fun getFeed(xmlFileURL: String,
                         callBack: (RssFeedResponse?) -> Unit) {

        // 1 Creates new instance of OkHttpClient
        val client = OkHttpClient()
        // 2 Builds an HTTP request object in order to make a call with OkHttpClient
        val request = Request.Builder()
                .url(xmlFileURL)
                .build()
        // 3 Passes Request object into the client through newCall() method
        // which then returns a Call object.
        client.newCall(request).enqueue(object : Callback {
            // 4 Define onFailure() to handle the call from OkHttp if the Request fails.
            // The main callBack method is called with null to indicate a failure.
            override fun onFailure(call: Call, e: IOException) {
                callBack(null)
            }
            // 5 If the Request succeeds, onResponse() is called by OkHttp.
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                // 6 Checks response for succeeds. Also checking to see if the server hosting
                // the RSS file returned an HTTP status code in the 200s
                if (response.isSuccessful) {
                    // 7 Checks response body for null
                    response.body()?.let { responseBody ->
                        // Provides a factor that can be used to obtain a parser for XML documents
                        // and creates a new instance called dBuilder.
                        // dBuilder.parse() is called with the RSS file content stream and the
                        // resulting top level XML Document is assigned to doc.
                        val dbFactory = DocumentBuilderFactory.newInstance()
                        val dBuilder = dbFactory.newDocumentBuilder()
                        val doc = dBuilder.parse(responseBody.byteStream())

                        // This creates a new empty RssFeedResponse
                        val rssFeedResponse = RssFeedResponse(episodes = mutableListOf())
                        // Called to parse RSS document into the rssFeeDResponse object
                        domToRssFeedResponse(doc, rssFeedResponse)
                        // Passes rssFeedResponse to callBack method and prints out the result
                        callBack(rssFeedResponse)
                        println(rssFeedResponse)

                        // Parse response and send to callback
                        return
                    }
                }
                callBack(null)
            }
        })

    }
    private fun domToRssFeedResponse(node: Node, rssFeedResponse: RssFeedResponse) {
        // 1 Checks nodeType to make sure it's an XML element.
        if (node.nodeType == Node.ELEMENT_NODE) {
            // 2 Store node's name and parent name. The name of the parent node will determine
                // where the current node resides in the tree.
            val nodeName = node.nodeName
            val parentName = node.parentNode.nodeName

            // 1 This is used to know the name of the parent of the parent, or grandparent node
            val grandParentName = node.parentNode.parentNode?.nodeName ?: ""
            // 2 If this node is a child of an item node, and item node is a child of a channel
            // node, then you know it is an episode element.
            if (parentName == "item" && grandParentName == "channel") {
                // 3  Assigns currentItem to the last episode in the episodes list
                val currentItem = rssFeedResponse.episodes?.last()
                if (currentItem != null) {
                    // 4 When expression used to switch on current node's name
                        // Populates current episode item's details from the node's textContent
                            // property
                    when (nodeName) {
                        "title" -> currentItem.title = node.textContent
                        "description" -> currentItem.description =
                                node.textContent
                        "itunes:duration" -> currentItem.duration =
                                node.textContent
                        "guid" -> currentItem.guid = node.textContent
                        "pubDate" -> currentItem.pubDate = node.textContent
                        "link" -> currentItem.link = node.textContent
                        "enclosure" -> {
                            currentItem.url = node.attributes.getNamedItem("url")
                                    .textContent
                            currentItem.type = node.attributes.getNamedItem("type")
                                    .textContent
                        }
                    }
                }
            }


            // 3 If the current node is a child of the channel node, it extracts the
            // top level RSS feed information from this node
            if (parentName == "channel") {
                // 4 when expression is used to switchon the nodeName
                when (nodeName) {
                    "title" -> rssFeedResponse.title = node.textContent
                    "description" -> rssFeedResponse.description =
                            node.textContent
                    "itunes:summary" -> rssFeedResponse.summary = node.textContent
                    "item" -> rssFeedResponse.episodes?.
                    add(RssFeedResponse.EpisodeResponse())
                    "pubDate" -> rssFeedResponse.lastUpdated =
                            DateUtils.xmlDateToDate(node.textContent)
                }
            }
        }
        // 5  assign nodeList to the list of child nodes for the current node
        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val childNode = nodeList.item(i)
            // 6 For each child node, domToRssFeedResponse() is called, passing in the existing
            //rssFeedResponse object.
            domToRssFeedResponse(childNode, rssFeedResponse)
        }
    }
}
interface FeedService {
    // 1 takes a URL pointing to an RSS file and a callback method. After the file is loaded and parsed,
    // the callback method gets called with the final RSS feed response
    fun getFeed(xmlFileURL: String,
                callBack: (RssFeedResponse?) -> Unit)
    // 2 Use a companion object to provide a singleton instance of the FeedService.
    companion object {
        val instance: FeedService by lazy {
            RssFeedService()
        }
    }
}