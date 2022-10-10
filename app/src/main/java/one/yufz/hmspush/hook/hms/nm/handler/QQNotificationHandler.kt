package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.app.Notification.InboxStyle
import android.content.Context
import one.yufz.hmspush.hook.XLog
import one.yufz.hmspush.hook.hms.nm.INotificationManager
import one.yufz.hmspush.hook.hms.nm.SystemNotificationManager
import one.yufz.hmspush.hook.util.getInboxLines
import one.yufz.hmspush.hook.util.getText
import one.yufz.hmspush.hook.util.getTitle
import one.yufz.hmspush.hook.util.getUserId
import one.yufz.hmspush.hook.util.newBuilder

class QQNotificationHandler : NotificationHandler {
    companion object {
        private const val TAG = "QQNotificationHandler"
    }

    override fun careAbout(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification): Boolean {
        return manager is SystemNotificationManager && packageName == "com.tencent.mobileqq"
    }

    override fun handle(chain: NotificationHandler.Chain, manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        XLog.d(TAG, "handle() called with: packageName = $packageName, id = $id, notification = $notification")

        val activeNotification = manager.getActiveNotifications(packageName, context.getUserId())
            .find { it.id == id }

        if (activeNotification != null) {
            val current = activeNotification.notification

            val lines = current.getInboxLines() ?: arrayOf(current.getText())

            val inboxStyle = InboxStyle()
                .setBigContentTitle(notification.getTitle())
                .setSummaryText("${lines.size + 1} 条消息")
                .addLine(notification.getText())
            lines.forEach(inboxStyle::addLine)

            val newNotification = notification.newBuilder(context)
                .setStyle(inboxStyle)
                .build()

            super.handle(chain, manager, context, packageName, id, newNotification)
        }
        super.handle(chain, manager, context, packageName, id, notification)
    }
}