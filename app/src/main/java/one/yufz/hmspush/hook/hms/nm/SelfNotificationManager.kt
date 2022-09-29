package one.yufz.hmspush.hook.hms.nm

import android.app.AndroidAppHelper
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class SelfNotificationManager : INotificationManager {
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
        notificationManager.createNotificationChannels(channels)
    }

    override fun cancelNotification(context: Context, packageName: String, id: Int) {
        notificationManager.cancel(id)
    }

    override fun deleteNotificationChannel(packageName: String, channelId: String) {
        notificationManager.deleteNotificationChannel(channelId)
    }
}