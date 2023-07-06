package one.yufz.hmspush.hook.hms.nm

import android.app.AndroidAppHelper
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import de.robv.android.xposed.XposedHelpers
import one.yufz.hmspush.common.ANDROID_PACKAGE_NAME
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.callMethod
import one.yufz.xposed.callStaticMethod
import org.lsposed.hiddenapibypass.HiddenApiBypass

class SystemNotificationManager : INotificationManager {
    companion object {
        private const val TAG = "SystemNotificationManag"
    }

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
            XposedHelpers.findMethodExact(
                notificationManager.javaClass,
                "getNotificationChannelForPackage",
                String::class.java,
                Int::class.java,
                String::class.java,
                String::class.java,
                Boolean::class.java
            )
                .invoke(notificationManager, packageName, getUid(packageName), channelId, null, boolean) as NotificationChannel?
        } else {
            //NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted);
            XposedHelpers.findMethodExact(notificationManager.javaClass, "getNotificationChannelForPackage", String::class.java, Int::class.java, String::class.java, Boolean::class.java)
                .invoke(notificationManager, packageName, getUid(packageName), channelId, false) as NotificationChannel?
        }
    }

    override fun notify(context: Context, packageName: String, id: Int, notification: Notification) {
        //enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId)
        val methodEnqueueNotificationWithTag = XposedHelpers.findMethodExact(
            notificationManager.javaClass,
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
        val channelsList = XposedHelpers.findConstructorExact("android.content.pm.ParceledListSlice", null, List::class.java)
            .newInstance(channels)
        notificationManager.callMethod("createNotificationChannelsForPackage", packageName, getUid(packageName), channelsList)
    }

    override fun cancelNotification(context: Context, packageName: String, id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //void cancelNotificationWithTag(String pkg, String opPkg, String tag, int id, int userId);
            val methodCancelNotificationWithTag =
                XposedHelpers.findMethodExact(notificationManager.javaClass, "cancelNotificationWithTag", String::class.java, String::class.java, String::class.java, Int::class.java, Int::class.java)
            methodCancelNotificationWithTag.invoke(notificationManager, packageName, ANDROID_PACKAGE_NAME, null, id, getUserId(context))
        } else {
            //  public void cancelNotificationWithTag(String pkg, String tag, int id, int userId)
            val methodCancelNotificationWithTag =
                XposedHelpers.findMethodExact(notificationManager.javaClass, "cancelNotificationWithTag", String::class.java, String::class.java, Int::class.java, Int::class.java)
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

    override fun getNotificationChannels(packageName: String, userId: Int): List<NotificationChannel> {
        //ParceledListSlice getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted);
        val parceledListSlice = notificationManager.callMethod("getNotificationChannelsForPackage", packageName, getUid(packageName), false)
        val list = parceledListSlice?.callMethod("getList") as? List<NotificationChannel>?
        return list ?: emptyList()
    }

    override fun clearHmsNotificationChannels(packageName: String, userId: Int) {
        getNotificationChannels(packageName, userId).filter { it.id.startsWith("com.huawei.hms.pushagent") }.forEach {
            XLog.d(TAG, "delete channel name: ${it.name}, channelId: ${it.id}")
            deleteNotificationChannel(packageName, it.id)
        }
    }
}