package com.brandonbaylosis.podplay.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandonbaylosis.podplay.R
import com.brandonbaylosis.podplay.adapter.EpisodeListAdapter
import com.brandonbaylosis.podplay.viewmodel.PodcastViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_podcast_details.*

class PodcastDetailsFragment : Fragment() {
    private val podcastViewModel: PodcastViewModel by activityViewModels()
    private lateinit var episodeListAdapter: EpisodeListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1 Call to setHasOptionsMenu() tells Android that this Fragment wants to add
        // items to the options menu. This causes the Fragment to receive a call to
        // onCreateOptionsMenu()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(
            R.layout.fragment_podcast_details,
            container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupControls()
        updateControls()
    }

    // 2 onCreateOptionsMenu() inflates the menu_details options menu so its items
    //are added to the podcast Activity menu.
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_details, menu)
    }

    private fun updateControls() {
        val viewData = podcastViewModel.activePodcastViewData ?:
        return
        feedTitleTextView.text = viewData.feedTitle
        feedDescTextView.text = viewData.feedDesc
        activity?.let { activity ->
            Glide.with(activity).load(viewData.imageUrl)
                .into(feedImageView)
        }
    }

    companion object {
        fun newInstance(): PodcastDetailsFragment {
            return PodcastDetailsFragment()
        }
    }

    private fun setupControls() {
        // 1 Allows feed title to scroll if it gets too long for the container
        feedDescTextView.movementMethod = ScrollingMovementMethod()
        // 2 Setup code for episode list RecyclerView
        episodeRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        episodeRecyclerView.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(
            episodeRecyclerView.context, layoutManager.orientation)
        episodeRecyclerView.addItemDecoration(dividerItemDecoration)
        // 3 Create the EpisodelistAdapter with the list of episodes in
        // activePodcastViewData and assign it to episodeRecyclerView.
        episodeListAdapter = EpisodeListAdapter(
            podcastViewModel.activePodcastViewData?.episodes)
        episodeRecyclerView.adapter = episodeListAdapter
    }
}