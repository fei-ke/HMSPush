package one.yufz.hmspush.settings

import android.app.ListFragment
import android.os.Bundle

class AppListFragment : ListFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = AppListAdapter()
    }
}