package com.huawei.android.app

import android.app.AndroidAppHelper
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import one.yufz.hmspush.HMS_PACKAGE_NAME
import one.yufz.hmspush.callMethod
import one.yufz.hmspush.callStaticMethod
import de.robv.android.xposed.XposedHelpers

class SystemNotificationManager : INotificationManager {

    private val notificationManager: Any = NotificationManager::class.java.callStaticMethod("getService")!!

    private fun getUid(packageName: String): Int {
        return AndroidAppHelper.currentApplication().packageManager.getPackageUid(packageName, 0)
    }

    override fun areNotificationsEnabled(packageName: String, userId: Int): Boolean {
        return notificationManager.callMethod("areNotificationsEnabledForPackage", packageName, getUid(packageName)) as Boolean
    }

    override fun getNotificationChannel(packageName: String, userId: Int, channelId: String, boolean: Boolean): NotificationChannel? {
        return notificationManager.callMethod("getNotificationChannel", HMS_PACKAGE_NAME, userId, packageName, channelId) as NotificationChannel?
    }

    override fun notify(context: Context, packageName: String, id: Int, notification: Notification) {
        context.getSystemService(NotificationManager::class.java)
            .notifyAsPackage(packageName, null, id, notification)
    }

    override fun createNotificationChannels(packageName: String, userId: Int, channels: List<NotificationChannel>) {
        val channelsList = XposedHelpers.findConstructorExact("android.content.pm.ParceledListSlice", null, List::class.java)
            .newInstance(channels)
        notificationManager.callMethod("createNotificationChannelsForPackage", packageName, getUid(packageName), channelsList)
    }

    override fun cancelNotification(context: Context, packageName: String, id: Int) {
        context.getSystemService(NotificationManager::class.java)
            .cancelAsPackage(packageName, null, id)
    }

    override fun deleteNotificationChannel(packageName: String, channelId: String) {
        notificationManager.callMethod("deleteNotificationChannel", packageName, channelId)
    }

}