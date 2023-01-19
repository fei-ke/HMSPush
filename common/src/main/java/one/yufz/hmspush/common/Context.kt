package one.yufz.hmspush.common

import android.content.Context

fun Context.dp2px(dp: Number): Int {
    val scale = resources.displayMetrics.density
    return (dp.toFloat() * scale + 0.5f).toInt()
}