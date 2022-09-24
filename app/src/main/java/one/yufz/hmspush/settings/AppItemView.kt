package one.yufz.hmspush.settings

import android.annotation.SuppressLint
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
import one.yufz.hmspush.HMS_PACKAGE_NAME
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
        setPadding(16.dp, 8.dp, 8.dp, 16.dp)

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

        more = child {
            setPadding(4.dp, 4.dp, 4.dp, 4.dp)
            setImageResource(R.drawable.ic_more)
            imageTintList = ColorStateList.valueOf(COLOR_GRAY)
        }
    }

    @SuppressLint("SetTextI18n")
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
            val registerInfo = if (item.registered) "已注册" else "未注册"
            val lastPushInfo = item.lastPushTime?.let { "  •  最近推送：${DateUtils.getRelativeTimeSpanString(it)}" } ?: ""
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
            menu.add("取消注册").setOnMenuItemClickListener {
                showUnregisterDialog(item)
                true
            }
        }

        menu.add("启动").setOnMenuItemClickListener {
            Util.launchApp(context, item.packageName)
            true
        }

        menu.add("应用信息").setOnMenuItemClickListener {
            Util.launchAppInfo(context, item.packageName)
            true
        }
        popupMenu.show()
    }

    private fun showUnregisterDialog(item: AppInfo) {
        AlertDialog.Builder(context)
            .setTitle("确定取消注册")
            .setPositiveButton("确定") { _, _ -> Util.unregisterPush(item.packageName) }
            .setNegativeButton("取消", null)
            .show()
    }
}