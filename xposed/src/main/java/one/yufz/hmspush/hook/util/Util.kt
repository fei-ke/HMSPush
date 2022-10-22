package one.yufz.hmspush.hook.util

import android.content.Context
import one.yufz.xposed.callMethod

fun Context.getUserId(): Int {
    return callMethod("getUserId") as Int? ?: 0
}