package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.content.Context
import one.yufz.hmspush.hook.hms.nm.INotificationManager

interface NotificationHandler {
    fun careAbout(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification): Boolean {
        return false
    }

    fun handle(chain: Chain, manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        chain.proceed(manager, context, packageName, id, notification)
    }

    interface Chain {
        fun proceed(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification)
    }
}