package one.yufz.hmspush.settings

import android.app.ListFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.Toolbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        view.setBackgroundColor(Color.WHITE)
        val context = requireNotNull(context)

        val listContainer = listView.parent as FrameLayout
        val toolbar = listContainer.child<Toolbar>(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
            title = "HMS Push"
            setTitleTextColor(context.getColor(android.R.color.primary_text_light))
            setBackgroundColor(Color.WHITE)
            fitsSystemWindows = true
        }

        toolbar.setOnApplyWindowInsetsListener { v, insets ->
            val toolBarHeight = context.dp2px(48) + insets.systemWindowInsetTop
            toolbar.layoutParams.height = toolBarHeight
            toolbar.setPadding(0, insets.systemWindowInsetTop, 0, 0)
            listView.layoutParams = (listView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = toolBarHeight
            }
            insets
        }
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                val firstChild = view.getChildAt(0)
                if (firstVisibleItem == 0 && firstChild != null && firstChild.top == 0) {
                    toolbar.elevation = 0f
                } else {
                    toolbar.elevation = context.dp2px(2).toFloat()
                }
            }
        })
        listView.divider = null
        listView.selector = ColorDrawable(Color.TRANSPARENT)

        viewModel.observeAppList().onEach {
            (listAdapter as AppListAdapter).updateData(it)
        }.launchIn(mainScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
        viewModel.onDestroy()
    }

}