package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import android.app.Notification
import android.app.NotificationChannel
import com.huawei.android.app.NotificationManagerEx
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.findClass
import one.yufz.xposed.hookMethod

object HookPushNC {
    private const val TAG = "HookPushNC"

    fun canHook(classLoader: ClassLoader): Boolean {
        return try {
            classLoader.findClass("com.huawei.hsf.notification.HwNotificationManager")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    fun hook(classLoader: ClassLoader) {
        XLog.d(TAG, "hookPushNC() called with: classLoader = $classLoader")

        FakeHsf.hook(classLoader)

        PushSignWatcher.watch()

        val classHwNotificationManager = classLoader.findClass("com.huawei.hsf.notification.HwNotificationManager")
        val classHsfApi = classLoader.findClass("com.huawei.hsf.common.api.HsfApi")

        //change default priority of notification
        classLoader.findClass("com.huawei.hms.pushnc.entity.PushSelfShowMessage").hookMethod("getPriority") {
            replace { 1 }
        }

        classHwNotificationManager.hookMethod("isSupportHmsNc", classHsfApi) {
            replace { true }
        }

        //boolean areNotificationsEnabled(HsfApi hsfApi, String packageName, int userId)
        classHwNotificationManager.hookMethod("areNotificationsEnabled", classHsfApi, String::class.java, Int::class.java) {
            replace { NotificationManagerEx.areNotificationsEnabled(args[1] as String, args[2] as Int) }
        }

        //boolean cancelNotification(HsfApi hsfApi, String packageName, int id, int userId)
        classHwNotificationManager.hookMethod("cancelNotification", classHsfApi, String::class.java, Int::class.java, Int::class.java) {
            replace {
                NotificationManagerEx.cancelNotification(AndroidAppHelper.currentApplication(), args[1] as String, args[2] as Int)
                return@replace true
            }

        }
        //boolean createNotificationChannels(HsfApi hsfApi, String packageName, int userId, List<NotificationChannel> list)
        classHwNotificationManager.hookMethod("createNotificationChannels", classHsfApi, String::class.java, Int::class.java, List::class.java) {
            replace {
                NotificationManagerEx.createNotificationChannels(args[1] as String, args[2] as Int, args[3] as List<NotificationChannel>)
                return@replace true
            }
        }
        //boolean deleteNotificationChannel(HsfApi hsfApi, String packageName String channelId)
        classHwNotificationManager.hookMethod("deleteNotificationChannel", classHsfApi, String::class.java, String::class.java) {
            replace {
                NotificationManagerEx.deleteNotificationChannel(args[1] as String, args[2] as String)
                return@replace true
            }
        }

        //NotificationChannel getNotificationChannels(HsfApi hsfApi, String packageName, int userId, String channelId)
        classHwNotificationManager.hookMethod("getNotificationChannels", classHsfApi, String::class.java, Int::class.java, String::class.java) {
            replace { NotificationManagerEx.getNotificationChannel(args[1] as String, args[2] as Int, args[3] as String, false) }
        }

        //boolean notify(HsfApi hsfApi, String packageName, int id, int userId, Notification notification)
        classHwNotificationManager.hookMethod("notify", classHsfApi, String::class.java, Int::class.java, Int::class.java, Notification::class.java) {
            replace {
                NotificationManagerEx.notify(AndroidAppHelper.currentApplication(), args[1] as String, args[2] as Int, args[4] as Notification)
                return@replace true
            }
        }
    }
}