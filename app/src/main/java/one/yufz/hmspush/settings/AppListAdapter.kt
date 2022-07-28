package one.yufz.hmspush.settings

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class AppListAdapter : BaseAdapter() {
    private var data: List<AppInfo> = emptyList()

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): AppInfo {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        return TextView(parent.context).apply {
            text = item.packageName
        }
    }
}