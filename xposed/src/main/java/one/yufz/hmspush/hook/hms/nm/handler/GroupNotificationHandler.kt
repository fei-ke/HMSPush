package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.content.Context
import one.yufz.hmspush.hook.XLog
import one.yufz.hmspush.hook.hms.nm.INotificationManager
import one.yufz.hmspush.hook.hms.nm.SelfNotificationManager
import one.yufz.xposed.set

class GroupNotificationHandler : NotificationHandler {
    companion object {
        private const val TAG = "GroupNotificationHandle"
    }

    override fun careAbout(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification): Boolean {
        return manager is SelfNotificationManager
    }

    override fun handle(chain: NotificationHandler.Chain, manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        XLog.d(TAG, "handle() called with: packageName = $packageName, id = $id, notification = $notification")

        notification["mGroupKey"] = packageName

        super.handle(chain, manager, context, packageName, id, notification)

        val groupNotification = notification.clone().apply {
            this.flags = flags.or(Notification.FLAG_GROUP_SUMMARY)
            this["mGroupAlertBehavior"] = Notification.GROUP_ALERT_CHILDREN
        }

        manager.notify(context, packageName, packageName.hashCode(), groupNotification)
    }
}