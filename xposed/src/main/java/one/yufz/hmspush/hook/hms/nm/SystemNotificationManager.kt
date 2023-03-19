package one.yufz.hmspush.hook.hms.nm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.StatusBarNotification
import one.yufz.hmspush.common.ANDROID_PACKAGE_NAME
import one.yufz.xposed.AndroidAppHelper
import one.yufz.xposed.callMethod
import one.yufz.xposed.callStaticMethod
import one.yufz.xposed.findClass
import one.yufz.xposed.findConstructorExact
import one.yufz.xposed.findMethodExact
import org.lsposed.hiddenapibypass.HiddenApiBypass

class SystemNotificationManager : INotificationManager {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }
    }

    private val notificationManager: Any = NotificationManager::class.java.callStaticMethod("getService")!!

    private fun getUid(packageName: String): Int {
        return AndroidAppHelper.currentApplication().packageManager.getPackageUid(packageName, 0)
    }

    private fun getUserId(context: Context): Int {
        return context.callMethod("getUserId") as Int? ?: 0
    }

    override fun areNotificationsEnabled(packageName: String, userId: Int): Boolean {
        return notificationManager.callMethod("areNotificationsEnabledForPackage", packageName, getUid(packageName)) as Boolean
    }

    override fun getNotificationChannel(packageName: String, userId: Int, channelId: String, boolean: Boolean): NotificationChannel? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, String conversationId, boolean includeDeleted);
            notificationManager.javaClass.findMethodExact("getNotificationChannelForPackage", String::class.java, Int::class.java, String::class.java, String::class.java, Boolean::class.java)
                .invoke(notificationManager, packageName, getUid(packageName), channelId, null, boolean) as NotificationChannel?
        } else {
            //NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted);
            notificationManager.javaClass.findMethodExact("getNotificationChannelForPackage", String::class.java, Int::class.java, String::class.java, Boolean::class.java)
                .invoke(notificationManager, packageName, getUid(packageName), channelId, false) as NotificationChannel?
        }
    }

    override fun notify(context: Context, packageName: String, id: Int, notification: Notification) {
        //enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId)
        val methodEnqueueNotificationWithTag = notificationManager.javaClass.findMethodExact(
            "enqueueNotificationWithTag",
            String::class.java,
            String::class.java,
            String::class.java,
            Int::class.java,
            Notification::class.java,
            Int::class.java
        )
        val opPkg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ANDROID_PACKAGE_NAME else packageName
        methodEnqueueNotificationWithTag.invoke(notificationManager, packageName, opPkg, null, id, notification, getUserId(context))
    }

    override fun createNotificationChannels(packageName: String, userId: Int, channels: List<NotificationChannel>) {
        //append [HMS] to channel name
        channels.forEach {
            it.name = "[HMS]${it.name}"
        }
        val channelsList = findClass("android.content.pm.ParceledListSlice").findConstructorExact(List::class.java)
            .newInstance(channels)
        notificationManager.callMethod("createNotificationChannelsForPackage", packageName, getUid(packageName), channelsList)
    }

    override fun cancelNotification(context: Context, packageName: String, id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //void cancelNotificationWithTag(String pkg, String opPkg, String tag, int id, int userId);
            val methodCancelNotificationWithTag =
                notificationManager.javaClass.findMethodExact("cancelNotificationWithTag", String::class.java, String::class.java, String::class.java, Int::class.java, Int::class.java)
            methodCancelNotificationWithTag.invoke(notificationManager, packageName, ANDROID_PACKAGE_NAME, null, id, getUserId(context))
        } else {
            //  public void cancelNotificationWithTag(String pkg, String tag, int id, int userId)
            val methodCancelNotificationWithTag =
                notificationManager.javaClass.findMethodExact("cancelNotificationWithTag", String::class.java, String::class.java, Int::class.java, Int::class.java)
            methodCancelNotificationWithTag.invoke(notificationManager, packageName, null, id, getUserId(context))
        }
    }

    override fun deleteNotificationChannel(packageName: String, channelId: String) {
        notificationManager.callMethod("deleteNotificationChannel", packageName, channelId)
    }

    override fun getActiveNotifications(packageName: String, userId: Int): Array<StatusBarNotification> {
        val parceledListSlice = notificationManager.callMethod("getAppActiveNotifications", packageName, userId)
        val list = parceledListSlice?.callMethod("getList") as List<StatusBarNotification>
        return list.toTypedArray()
    }
}