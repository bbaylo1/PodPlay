package com.brandonbaylosis.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandonbaylosis.podplay.R
import com.brandonbaylosis.podplay.adapter.PodcastListAdapter
import com.brandonbaylosis.podplay.viewmodel.PodcastViewModel
import com.brandonbaylosis.podplay.viewmodel.SearchViewModel
import com.brandonbaylosis.repository.ItunesRepo
import com.brandonbaylosis.repository.PodcastRepo
import com.brandonbaylosis.service.ItunesService
import kotlinx.android.synthetic.main.activity_podcast.*

class PodcastActivity : AppCompatActivity(),
    PodcastListAdapter.PodcastListAdapterListener {
    private val searchViewModel by viewModels<SearchViewModel>()
    private lateinit var podcastListAdapter: PodcastListAdapter
    private lateinit var searchMenuItem: MenuItem
    // initializes the podcastViewModel object when the Activity is created.
    private val podcastViewModel by viewModels<PodcastViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast)

        val TAG = javaClass.simpleName
        val itunesService = ItunesService.instance
        val itunesRepo = ItunesRepo(itunesService)
        // Uses ItunesRepo to search for the podcast and prints the results to the
        // Logcat window
        itunesRepo.searchByTerm("Android Developer") {
            Log.i(TAG, "Results = $it")
        }
        setupToolbar()
        setupViewModels()
        updateControls()
        handleIntent(intent)
        addBackStackListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // 1
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)
        // 2
        searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem.actionView as SearchView
        // 3
        val searchManager = getSystemService(Context.SEARCH_SERVICE)
                as SearchManager
        // 4
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        // Creates activity again if device is rotated
        // Hides podcastRecyclerView when onCreateOptionsMenu is called
        // and there's any fragments on the back stack
        if (supportFragmentManager.backStackEntryCount > 0) {
            podcastRecyclerView.visibility = View.INVISIBLE
        }
        // Ensures that the searchMenuItem remains hidden if podcastRecyclerView is
        //not visible
        if (podcastRecyclerView.visibility == View.INVISIBLE) {
            searchMenuItem.isVisible = false
        }
        return true
    }

    private fun performSearch(term: String) {
        showProgressBar()
        searchViewModel.searchPodcasts(term) { results ->
            hideProgressBar()
            toolbar.title = term
            podcastListAdapter.setSearchData(results)
        }
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY) ?:
            return
            performSearch(query)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupViewModels() {
        val service = ItunesService.instance
        searchViewModel.iTunesRepo = ItunesRepo(service)
        // new instance of PodcastRepo is assigned to the podcastViewModel.podcastRepo property
        podcastViewModel.podcastRepo = PodcastRepo()
    }

    private fun updateControls() {
        podcastRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        podcastRecyclerView.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(
            podcastRecyclerView.context, layoutManager.orientation)
        podcastRecyclerView.addItemDecoration(dividerItemDecoration)
        podcastListAdapter = PodcastListAdapter(null, this, this)
        podcastRecyclerView.adapter = podcastListAdapter
    }

    // Called when user taps on a podcast
    override fun onShowDetails(podcastSummaryViewData:
                               SearchViewModel.PodcastSummaryViewData) {
        // 1
        val feedUrl = podcastSummaryViewData.feedUrl ?: return
        // 2
        showProgressBar()
        // 3
        podcastViewModel.getPodcast(podcastSummaryViewData) {
            // 4
            hideProgressBar()
            if (it != null) {
                // 5
                showDetailsFragment()
            } else {
                // 6
                showError("Error loading feed $feedUrl")
            }
        }
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }
    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    companion object {
        private const val TAG_DETAILS_FRAGMENT = "DetailsFragment"
    }

    private fun createPodcastDetailsFragment():
            PodcastDetailsFragment {
        // 1
        var podcastDetailsFragment = supportFragmentManager
            .findFragmentByTag(TAG_DETAILS_FRAGMENT) as
                PodcastDetailsFragment?
        // 2
        if (podcastDetailsFragment == null) {
            podcastDetailsFragment =
                PodcastDetailsFragment.newInstance()
        }
        return podcastDetailsFragment
    }

    private fun showDetailsFragment() {
        // 1
        val podcastDetailsFragment = createPodcastDetailsFragment()
        // 2
        supportFragmentManager.beginTransaction().add(
            R.id.podcastDetailsContainer,
            podcastDetailsFragment, TAG_DETAILS_FRAGMENT)
            .addToBackStack("DetailsFragment").commit()
        // 3
        podcastRecyclerView.visibility = View.INVISIBLE
        // 4
        searchMenuItem.isVisible = false
    }

    // Displays generic alert dialog with an error message
    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_button), null)
            .create()
            .show()
    }

    private fun addBackStackListener()
    {
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                podcastRecyclerView.visibility = View.VISIBLE
            }
        }
    }

}