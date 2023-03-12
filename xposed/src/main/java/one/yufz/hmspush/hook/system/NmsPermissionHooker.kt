package one.yufz.hmspush.hook.system

import android.app.AndroidAppHelper
import android.app.Notification
import android.os.Binder
import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import one.yufz.hmspush.common.ANDROID_PACKAGE_NAME
import one.yufz.hmspush.common.HMS_PACKAGE_NAME
import one.yufz.xposed.HookCallback
import one.yufz.xposed.findClass
import one.yufz.xposed.findMethodExact
import one.yufz.xposed.hook

object NmsPermissionHooker {
    private fun fromHms() = try {
        Binder.getCallingUid() == AndroidAppHelper.currentApplication().packageManager.getPackageUid(HMS_PACKAGE_NAME, 0)
    } catch (e: Throwable) {
        false
    }

    private fun tryHookPermission(packageName: String): Boolean {
        if (packageName != HMS_PACKAGE_NAME && fromHms()) {
            Binder.clearCallingIdentity()
            return true
        }
        return false
    }

    private fun hookPermission(targetPackageNameParamIndex: Int, hookExtra: (XC_MethodHook.MethodHookParam.() -> Unit)? = null): HookCallback = {
        doBefore {
            if (tryHookPermission(args[targetPackageNameParamIndex] as String)) {
                hookExtra?.invoke(this)
            }
        }
    }

    fun hook(classINotificationManager: Class<*>) {
        //boolean areNotificationsEnabledForPackage(String pkg, int uid);
        classINotificationManager.findMethodExact("areNotificationsEnabledForPackage", String::class.java, Int::class.java)
            .hook(hookPermission(0))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, String conversationId, boolean includeDeleted);
            classINotificationManager.findMethodExact("getNotificationChannelForPackage", String::class.java, Int::class.java, String::class.java, String::class.java, Boolean::class.java)
                .hook(hookPermission(0))
        } else {
            //NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted);
            classINotificationManager.findMethodExact("getNotificationChannelForPackage", String::class.java, Int::class.java, String::class.java, Boolean::class.java)
                .hook(hookPermission(0))
        }

        //void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId)
        classINotificationManager.findMethodExact("enqueueNotificationWithTag", String::class.java, String::class.java, String::class.java, Int::class.java, Notification::class.java, Int::class.java)
            .hook(hookPermission(0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    args[1] = ANDROID_PACKAGE_NAME
                }
            })

        //void createNotificationChannelsForPackage(String pkg, int uid, in ParceledListSlice channelsList);
        classINotificationManager.findMethodExact("createNotificationChannelsForPackage", String::class.java, Int::class.java, findClass("android.content.pm.ParceledListSlice"))
            .hook(hookPermission(0))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //void cancelNotificationWithTag(String pkg, String opPkg, String tag, int id, int userId);
            classINotificationManager.findMethodExact("cancelNotificationWithTag", String::class.java, String::class.java, String::class.java, Int::class.java, Int::class.java)
                .hook(hookPermission(0) {
                    args[1] = ANDROID_PACKAGE_NAME
                })
        } else {
            //void cancelNotificationWithTag(String pkg, String opPkg, String tag, int id, int userId);
            classINotificationManager.findMethodExact("cancelNotificationWithTag", String::class.java, String::class.java, Int::class.java, Int::class.java)
                .hook(hookPermission(0))
        }

        //void deleteNotificationChannel(String pkg, String channelId);
        classINotificationManager.findMethodExact("deleteNotificationChannel", String::class.java, String::class.java)
            .hook(hookPermission(0))

        //ParceledListSlice getAppActiveNotifications(String callingPkg, int userId);
        classINotificationManager.findMethodExact("getAppActiveNotifications", String::class.java, Int::class.java)
            .hook(hookPermission(0))
    }
}