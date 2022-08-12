package one.yufz.hmspush.settings

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

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
        val itemView = if (convertView is AppItemView) {
            convertView
        } else {
            AppItemView(parent.context)
        }

        itemView.bind(getItem(position))

        return itemView
    }

    fun updateData(list: List<AppInfo>) {
        data = list
        notifyDataSetChanged()
    }
}