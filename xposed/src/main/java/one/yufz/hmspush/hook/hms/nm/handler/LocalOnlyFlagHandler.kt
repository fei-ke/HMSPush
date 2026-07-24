package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.content.Context
import one.yufz.hmspush.hook.hms.Prefs
import one.yufz.hmspush.hook.hms.nm.INotificationManager
import one.yufz.hmspush.hook.util.newBuilder

class LocalOnlyFlagHandler : NotificationHandler {
    override fun careAbout(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification): Boolean {
        return Prefs.prefModel.forceRemoveLocalOnly
                && (notification.flags and Notification.FLAG_LOCAL_ONLY) != 0
    }

    override fun handle(chain: NotificationHandler.Chain, manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        val newNotification = notification.newBuilder(context).build()
        newNotification.flags = notification.flags and Notification.FLAG_LOCAL_ONLY.inv()
        chain.proceed(manager, context, packageName, id, newNotification)
    }
}