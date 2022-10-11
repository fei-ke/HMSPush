package one.yufz.hmspush.hook.util

import android.app.Notification
import android.content.Context
import one.yufz.xposed.newInstance

fun Notification.newBuilder(context: Context): Notification.Builder {
    return Notification.Builder::class.java.newInstance(context, this) as Notification.Builder
}

fun Notification.getText(): String? {
    return extras.getString(Notification.EXTRA_TEXT)
}

fun Notification.getTitle(): String? {
    return extras.getString(Notification.EXTRA_TITLE)
}

fun Notification.getInboxLines(): Array<CharSequence>? {
    return extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
}