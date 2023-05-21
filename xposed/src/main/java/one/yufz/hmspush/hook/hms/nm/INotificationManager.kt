package one.yufz.hmspush.hook.hms.nm

import android.app.Notification
import android.app.NotificationChannel
import android.content.Context
import android.service.notification.StatusBarNotification

interface INotificationManager {
    fun areNotificationsEnabled(packageName: String, userId: Int): Boolean
    fun getNotificationChannel(packageName: String, userId: Int, channelId: String, boolean: Boolean): NotificationChannel?
    fun notify(context: Context, packageName: String, id: Int, notification: Notification)
    fun createNotificationChannels(packageName: String, userId: Int, channels: List<NotificationChannel>)
    fun cancelNotification(context: Context, packageName: String, id: Int)
    fun deleteNotificationChannel(packageName: String, channelId: String)
    fun getActiveNotifications(packageName: String, userId: Int): Array<StatusBarNotification>
    fun getNotificationChannels(packageName: String, userId: Int): List<NotificationChannel>
    fun clearHmsNotificationChannels(packageName: String, userId: Int)
}