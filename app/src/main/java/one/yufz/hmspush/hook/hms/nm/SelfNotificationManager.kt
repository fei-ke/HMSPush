package one.yufz.hmspush.hook.hms.nm

import android.app.AndroidAppHelper
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.service.notification.StatusBarNotification
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
        notificationManager.notify(id, notification)
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

    override fun getActiveNotifications(packageName: String, userId: Int): Array<StatusBarNotification> {
        return notificationManager.activeNotifications
    }
}