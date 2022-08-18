package com.huawei.android.app

import android.app.AndroidAppHelper
import android.app.Notification
import android.app.NotificationChannel
import android.content.Context
import de.robv.android.xposed.XposedHelpers
import one.yufz.hmspush.IS_SYSTEM_HOOK_READY
import one.yufz.hmspush.PushHistory
import one.yufz.hmspush.READY
import one.yufz.hmspush.XLog
import java.lang.reflect.InvocationTargetException

object NotificationManagerEx {
    private const val TAG = "NotificationManagerEx"

    @JvmStatic
    fun getNotificationManager() = this

    private val notificationManager: INotificationManager = createNotificationManager()

    private fun createNotificationManager(): INotificationManager {
        return if (AndroidAppHelper.currentApplication().packageManager.getInstallerPackageName(IS_SYSTEM_HOOK_READY) == READY) {
            XLog.d(TAG, "use SystemNotificationManager")
            SystemNotificationManager()
        } else {
            XLog.d(TAG, "use SelfNotificationManager")
            SelfNotificationManager()
        }
    }

    fun areNotificationsEnabled(packageName: String, userId: Int): Boolean {
        XLog.d(TAG, "areNotificationsEnabled() called with: packageName = $packageName, userId = $userId")
        return tryInvoke { notificationManager.areNotificationsEnabled(packageName, userId) }
    }

    fun getNotificationChannel(packageName: String, userId: Int, channelId: String, boolean: Boolean): NotificationChannel? {
        XLog.d(TAG, "getNotificationChannel() called with: packageName = $packageName, userId = $userId, channelId = $channelId, boolean = $boolean")
        return tryInvoke { notificationManager.getNotificationChannel(packageName, userId, channelId, boolean) }
    }

    fun notify(context: Context, packageName: String, id: Int, notification: Notification) {
        XLog.d(TAG, "notify() called with: context = $context, packageName = $packageName, id = $id, notification = $notification")

        tryInvoke { notificationManager.notify(context, packageName, id, notification) }

        PushHistory.record(packageName)
    }

    fun createNotificationChannels(packageName: String, userId: Int, channels: List<NotificationChannel>) {
        XLog.d(TAG, "createNotificationChannels() called with: packageName = $packageName, userId = $userId, channels = $channels")
        tryInvoke { notificationManager.createNotificationChannels(packageName, userId, channels) }
    }

    fun cancelNotification(context: Context, packageName: String, id: Int) {
        XLog.d(TAG, "cancelNotification() called with: context = $context, packageName = $packageName, id = $id")
        tryInvoke { notificationManager.cancelNotification(context, packageName, id) }
    }

    fun deleteNotificationChannel(packageName: String, channelId: String) {
        XLog.d(TAG, "deleteNotificationChannel() called with: packageName = $packageName, channelId = $channelId")
        tryInvoke { notificationManager.deleteNotificationChannel(packageName, channelId) }
    }

    private inline fun <R> tryInvoke(invoke: () -> R): R {
        try {
            return invoke()
        } catch (e: XposedHelpers.InvocationTargetError) {
            XLog.e(TAG, "tryInvoke: ", e)
            XLog.e(TAG, "tryInvoke targetException: ", e.cause)
            throw e.cause ?: e
        } catch (e: InvocationTargetException) {
            XLog.e(TAG, "tryInvoke: ", e)
            XLog.e(TAG, "tryInvoke targetException: ", e.targetException)
            throw e.targetException ?: e
        } catch (e: Throwable) {
            XLog.e(TAG, "tryInvoke: ", e)
            XLog.e(TAG, "tryInvoke cause: ", e.cause)
            throw e.cause ?: e
        }
    }
}