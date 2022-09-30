package one.yufz.hmspush.app.settings

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import one.yufz.hmspush.R

class AppItemView(context: Context) : LinearLayout(context) {
    companion object {
        private val COLOR_GREEN = Color.parseColor("#4CAF50")
        private val COLOR_GRAY = Color.parseColor("#808080")
    }

    private val Number.dp: Int
        get() = context.dp2px(this)

    private lateinit var icon: ImageView
    private lateinit var title: TextView
    private lateinit var subTitle: TextView
    private lateinit var status: TextView
    private lateinit var more: ImageView

    init {
        setPadding(16.dp, 8.dp, 4.dp, 8.dp)

        icon = child(48.dp, 48.dp)


        child<LinearLayout>(0, LayoutParams.WRAP_CONTENT) {
            orientation = VERTICAL
            (layoutParams as LayoutParams).apply {
                weight = 1f
                leftMargin = 16.dp
            }

            child<LinearLayout> {
                title = child {
                    isSingleLine = true
                    ellipsize = TextUtils.TruncateAt.END
                    textSize = 16f
                }
            }

            subTitle = child {
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.END
                setTextColor(COLOR_GRAY)
            }

            status = child {
                (layoutParams as MarginLayoutParams).topMargin = 4.dp
                textSize = 13f
            }
        }

        more = child(36.dp, 36.dp) {
            scaleType = ImageView.ScaleType.CENTER
            setImageResource(R.drawable.ic_more)
            imageTintList = ColorStateList.valueOf(COLOR_GRAY)
        }
    }

    fun bind(item: AppInfo) {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(item.packageName, 0)
        icon.setImageDrawable(info.loadIcon(pm))

        with(title) {
            text = item.name
            setTextColor(if (item.registered) COLOR_GREEN else Color.BLACK)
        }

        with(subTitle) {
            text = item.packageName
        }

        with(status) {
            val registerInfo = if (item.registered) context.getString(R.string.registered) else context.getString(R.string.unregistered)
            val lastPushInfo = item.lastPushTime?.let { context.getString(R.string.latest_push, DateUtils.getRelativeTimeSpanString(it)) } ?: ""
            text = registerInfo + lastPushInfo
            setTextColor(if (item.registered) COLOR_GREEN else COLOR_GRAY)
        }

        with(more) {
            setOnClickListener { buildAndShowPopup(it, item) }
        }
    }

    private fun buildAndShowPopup(more: View, item: AppInfo) {
        val popupMenu = PopupMenu(context, more)
        val menu = popupMenu.menu
        if (item.registered) {
            menu.add(R.string.menu_unregister).setOnMenuItemClickListener {
                showUnregisterDialog(item)
                true
            }
        }

        menu.add(R.string.menu_launch).setOnMenuItemClickListener {
            Util.launchApp(context, item.packageName)
            true
        }

        menu.add(R.string.menu_app_info).setOnMenuItemClickListener {
            Util.launchAppInfo(context, item.packageName)
            true
        }
        popupMenu.show()
    }

    private fun showUnregisterDialog(item: AppInfo) {
        AlertDialog.Builder(context)
            .setTitle(R.string.dialog_confirm_unregister)
            .setPositiveButton(R.string.dialog_confirm) { _, _ -> Util.unregisterPush(context, item.packageName) }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }
}