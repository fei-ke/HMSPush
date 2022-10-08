package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.content.Context
import one.yufz.hmspush.hook.XLog
import one.yufz.hmspush.hook.hms.nm.INotificationManager
import one.yufz.hmspush.hook.hms.nm.SystemNotificationManager
import one.yufz.xposed.set

class QQNotificationHandler : NotificationHandler {
    companion object {
        private const val TAG = "QQNotificationHandler"
    }

    override fun careAbout(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification): Boolean {
        return packageName == "com.tencent.mobileqq"
    }

    override fun handle(chain: NotificationHandler.Chain, manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        XLog.d(TAG, "handle() called with: packageName = $packageName, id = $id, notification = $notification")

        notification["mGroupKey"] = id.toString()

        val uniqueId = System.currentTimeMillis().toInt()
        super.handle(chain, manager, context, packageName, uniqueId, notification)

        // GroupNotificationHandler will group notification for SelfNotificationManager
        if (manager is SystemNotificationManager) {
            val groupNotification = notification.clone().apply {
                this.flags = flags.or(Notification.FLAG_GROUP_SUMMARY)
                this["mGroupAlertBehavior"] = Notification.GROUP_ALERT_CHILDREN
            }

            manager.notify(context, packageName, id, groupNotification)
        }
    }
}