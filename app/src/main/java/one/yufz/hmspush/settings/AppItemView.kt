package one.yufz.hmspush.settings

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.text.format.DateUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

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
    private lateinit var pushHistory: TextView
    private lateinit var status: TextView

    init {
        setPadding(16.dp, 16.dp, 16.dp, 16.dp)

        icon = child(48.dp, 48.dp)

        child<LinearLayout>(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT) {
            orientation = VERTICAL
            (layoutParams as LayoutParams).leftMargin = 16.dp

            child<LinearLayout> {
                title = child(0, LayoutParams.WRAP_CONTENT) {
                    (layoutParams as LayoutParams).weight = 1f
                    isSingleLine = true
                    ellipsize = TextUtils.TruncateAt.END
                    textSize = 16f
                }

                status = child {
                    (layoutParams as MarginLayoutParams).leftMargin = 4.dp
                }
            }

            subTitle = child {
                (layoutParams as MarginLayoutParams).topMargin = 4.dp
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.END
                setTextColor(context.getColor(android.R.color.tertiary_text_light))
            }

            pushHistory = child {
                (layoutParams as MarginLayoutParams).topMargin = 4.dp
                textSize = 12f
            }
        }
    }

    fun bind(item: AppInfo) {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(item.packageName, 0)
        icon.setImageDrawable(info.loadIcon(pm))

        with(title) {
            text = info.loadLabel(pm)
            setTextColor(if (item.registered) COLOR_GREEN else Color.BLACK)
        }

        with(subTitle) {
            text = item.packageName
        }

        with(pushHistory) {
            text = item.lastPushTime?.let { "最近推送：${DateUtils.getRelativeTimeSpanString(it)}" }
            setTextColor(if (item.registered) COLOR_GREEN else COLOR_GRAY)
        }

        with(status) {
            text = if (item.registered) "已注册" else "未注册"
            setTextColor(if (item.registered) COLOR_GREEN else COLOR_GRAY)
        }


    }
}