package one.yufz.hmspush.settings

import android.content.Context

fun Context.dp2px(dp: Number): Int = (dp.toFloat() * resources.displayMetrics.density + 0.5f).toInt()
