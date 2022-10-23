package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.app.Notification.InboxStyle
import android.content.Context
import one.yufz.hmspush.hook.XLog
import one.yufz.hmspush.hook.hms.Prefs
import one.yufz.hmspush.hook.hms.nm.INotificationManager
import one.yufz.hmspush.hook.util.getInboxLines
import one.yufz.hmspush.hook.util.getSummaryText
import one.yufz.hmspush.hook.util.getText
import one.yufz.hmspush.hook.util.getTitle
import one.yufz.hmspush.hook.util.getUserId
import one.yufz.hmspush.hook.util.newBuilder

class GroupByIdHandler : NotificationHandler {
    companion object {
        private const val TAG = "GroupByIdHandler"
    }

    override fun careAbout(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification): Boolean {
        return Prefs.prefModel.groupMessageById
    }

    override fun handle(chain: NotificationHandler.Chain, manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        XLog.d(TAG, "handle() called with: packageName = $packageName, id = $id, notification = $notification")

        val activeNotification = manager.getActiveNotifications(packageName, context.getUserId())
            .find { it.id == id }

        if (activeNotification != null) {
            val current = activeNotification.notification

            val lines = current.getInboxLines()?.take(25) ?: listOf(current.getText())

            val inboxStyle = InboxStyle()
                .setBigContentTitle(notification.getTitle())
                .setSummaryText(generateSummary(current.getSummaryText()))
                .addLine(notification.getText())
            lines.forEach(inboxStyle::addLine)

            val newNotification = notification.newBuilder(context)
                .setStyle(inboxStyle)
                .build()

            super.handle(chain, manager, context, packageName, id, newNotification)
        } else {
            super.handle(chain, manager, context, packageName, id, notification)
        }
    }

    private fun generateSummary(current: CharSequence?): CharSequence {
        val currentCount = current?.split(" ")?.firstOrNull()?.toInt() ?: 1
        return "${currentCount + 1} 条消息"
    }
}