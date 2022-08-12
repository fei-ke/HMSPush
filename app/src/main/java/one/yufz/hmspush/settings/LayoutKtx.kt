package one.yufz.hmspush.settings

import android.view.View
import android.view.ViewGroup
import one.yufz.hmspush.newInstance

inline fun <reified T : View> ViewGroup.child(width: Int, height: Int, index: Int = -1, action: T.() -> Unit = {}): T {
    val params = ViewGroup.LayoutParams(width, height)
    return child(params, index, action)
}

inline fun <reified T : View> ViewGroup.child(params: ViewGroup.LayoutParams? = null, index: Int = -1, action: T.() -> Unit = {}): T {
    val child = T::class.java.newInstance(context) as T

    if (params != null) {
        addView(child, index, params)
    } else {
        addView(child, index)
    }

    child.action()

    return child
}