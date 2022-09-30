package one.yufz.hmspush.app.settings

import android.app.ListFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.SearchView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import one.yufz.hmspush.MainActivity
import one.yufz.hmspush.R

class AppListFragment : ListFragment() {
    companion object {
        private const val TAG = "AppListFragment"
    }

    private val viewModel by lazy { AppListViewModel(context) }
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = AppListAdapter()

        viewModel.onCreate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireNotNull(context)

        setHasOptionsMenu(true)
        activity.actionBar?.apply {
            setTitle(R.string.app_name)
            setDisplayHomeAsUpEnabled(false)
            setDisplayUseLogoEnabled(true)
        }
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                val firstChild = view.getChildAt(0)
                if (firstVisibleItem == 0 && firstChild != null && firstChild.top == 0) {
                    activity.actionBar?.elevation = 0f
                } else {
                    activity.actionBar?.elevation = context.dp2px(2).toFloat()
                }
            }
        })
        listView.divider = null
        listView.selector = ColorDrawable(Color.TRANSPARENT)

        viewModel.observeAppList().onEach {
            (listAdapter as AppListAdapter).updateData(it)
        }.launchIn(mainScope)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        setupSearchMenu(menu)
    }

    private fun setupSearchMenu(menu: Menu) {
        val searchView = SearchView(context)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filter(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filter(newText ?: "")
                return true
            }

        })

        menu.add(R.string.menu_search)
            .setIcon(R.drawable.ic_search)
            .setActionView(searchView)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_ALWAYS)

        menu.add(R.string.menu_settings)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
            .setOnMenuItemClickListener {
                (activity as MainActivity).pushFragment(SettingsFragment(), "settings")
                true
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
        viewModel.onDestroy()
    }

}