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
import com.brandonbaylosis.service.FeedService
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
        setupToolbar() // Gets ActionBar support for Activity
        setupViewModels()
        updateControls()
        handleIntent(intent)
        addBackStackListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // 1 Inflates options menu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)
        // 2 Search action menu item is found within the options menu, and the search
        //view is taken from the item’s actionView property.
        searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem.actionView as SearchView
        // 3 The system SearchManager object is loaded, and will be used
        // to load searchable info XML files
        val searchManager = getSystemService(Context.SEARCH_SERVICE)
                as SearchManager
        // 4 use searchManager to load the search configuration and assign it to the
        // searchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        // Creates activity again if device is rotated
        // Hides podcastRecyclerView when onCreateOptionsMenu is called
        // and there's any fragments on the back stack
        if (supportFragmentManager.backStackEntryCount > 0) {
            podcastRecyclerView.visibility = View.INVISIBLE
        }
        // Ensures that the searchMenuItem remains hidden if podcastRecyclerView is
        // not visible
        if (podcastRecyclerView.visibility == View.INVISIBLE) {
            searchMenuItem.isVisible = false
        }
        return true
    }

    // Uses SearchViewModel to find the podcasts based on the search term.
    private fun performSearch(term: String) {
        // Displays progress bar before search starts and hides it as soon as it's over
        showProgressBar()
        searchViewModel.searchPodcasts(term) { results ->
            hideProgressBar()
            // toolbar title is updated to the show the search term
            toolbar.title = term
            // RecyclerView Adapter is updated with the results
            podcastListAdapter.setSearchData(results)
        }
    }

    // Takes in an Intent and checks to see if it’s an ACTION_SEARCH. If so, it
    //extracts the search query string and passes it to performSearch()
    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY) ?:
            return
            performSearch(query)
        }
    }

    // Called when the Intent is sent from the search widget.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Called to make sure new Intent is saved within the Activity
        setIntent(intent)
        // Called to perform the search
        handleIntent(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupViewModels() {
        val service = ItunesService.instance
        searchViewModel.iTunesRepo = ItunesRepo(service)
        // new instance of PodcastRepo is assigned to the podcastViewModel.podcastRepo property
        val rssService = FeedService.instance
        podcastViewModel.podcastRepo = PodcastRepo(rssService)    }

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
        // 1 feedUrl taken from podcastSummaryViewData object if not null
        // method returns without doing anything otherwise
        val feedUrl = podcastSummaryViewData.feedUrl ?: return
        // 2 progress bar is displayed to show the user that the app is busy
        // loading the podcast data.
        showProgressBar()
        // 3 podcastViewModel.getPodcast() is called to load the podcast view data
        podcastViewModel.getPodcast(podcastSummaryViewData) {
            // 4 Hides progress bar after data is returned
            hideProgressBar()
            if (it != null) {
                // 5 If data is not null, then showDetailsFragment() is called to
                    // display the detail fragment
                showDetailsFragment()
            } else {
                // 6 If data null, dispaly error dialog
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
        // 1 Use supportFragmentManager.findFragmentByTag() to check if the
        // Fragment already exists.
        var podcastDetailsFragment = supportFragmentManager
            .findFragmentByTag(TAG_DETAILS_FRAGMENT) as
                PodcastDetailsFragment?
        // 2 If there’s no existing fragment, a new one is created using newInstance()
        // on the Fragment’s companion object
        if (podcastDetailsFragment == null) {
            podcastDetailsFragment =
                PodcastDetailsFragment.newInstance()
        }
        // Return Fragment object
        return podcastDetailsFragment
    }

    private fun showDetailsFragment() {
        // 1 Details fragment is created or retrieved from the fragment manager.
        val podcastDetailsFragment = createPodcastDetailsFragment()
        // 2 The fragment is added to the supportFragmentManager
        // TAG_DETAILS_FRAGMENT used to identify the fragment
        // .addToBackStack() used to make sure back button works to close the fragment
        supportFragmentManager.beginTransaction().add(
            R.id.podcastDetailsContainer,
            podcastDetailsFragment, TAG_DETAILS_FRAGMENT)
            .addToBackStack("DetailsFragment").commit()
        // 3 main podcast RecyclerView is hidden so the only thing showing is the detail Fragment
        podcastRecyclerView.visibility = View.INVISIBLE
        // 4 searchMenuItem is hidden so that the search icon is not shown on the details screen
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