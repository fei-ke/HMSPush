package one.yufz.hmspush.hook.hms.nm

import android.app.AndroidAppHelper
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.set

class SelfNotificationManager : INotificationManager {
    companion object {
        private const val TAG = "SelfNotificationManager"
    }

    private val notificationManager = AndroidAppHelper.currentApplication()
        .getSystemService(NotificationManager::class.java)

    override fun areNotificationsEnabled(packageName: String, userId: Int): Boolean {
        return true
    }

    override fun getNotificationChannel(packageName: String, userId: Int, channelId: String, boolean: Boolean): NotificationChannel? {
        return notificationManager.getNotificationChannel(channelId)
    }

    override fun notify(context: Context, packageName: String, id: Int, notification: Notification) {
        notification["mGroupKey"] = packageName
        notificationManager.notify(id, notification)
        updateGroupSummary(packageName, notification)
    }

    private fun updateGroupSummary(packageName: String, notification: Notification) {
        val group = notification.clone().apply {
            this.flags = flags.or(Notification.FLAG_GROUP_SUMMARY)
            this["mGroupAlertBehavior"] = Notification.GROUP_ALERT_CHILDREN
        }

        notificationManager.notify(packageName.hashCode(), group)
    }

    override fun createNotificationChannels(packageName: String, userId: Int, channels: List<NotificationChannel>) {
        channels.forEach { channel ->
            channel["mDesc"] = channel.name
            channel.name = getApplicationName(packageName) ?: packageName
        }
        notificationManager.createNotificationChannels(channels)
    }

    private fun getApplicationName(packageName: String): CharSequence? {
        try {
            val pm = AndroidAppHelper.currentApplication().packageManager
            return pm.getApplicationInfo(packageName, 0).loadLabel(pm)
        } catch (e: Throwable) {
            XLog.e(TAG, "getApplicationName: error", e)
            return null
        }
    }

    override fun cancelNotification(context: Context, packageName: String, id: Int) {
        notificationManager.cancel(id)
    }

    override fun deleteNotificationChannel(packageName: String, channelId: String) {
        notificationManager.deleteNotificationChannel(channelId)
    }
}