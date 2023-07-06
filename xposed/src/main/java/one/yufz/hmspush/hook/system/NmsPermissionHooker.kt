package one.yufz.hmspush.hook.system

import android.app.AndroidAppHelper
import android.app.Application
import android.app.Notification
import android.content.Context
import android.os.Binder
import android.os.Build
import android.os.Process
import android.os.UserHandle
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findMethodExact
import one.yufz.hmspush.common.ANDROID_PACKAGE_NAME
import one.yufz.hmspush.common.HMS_PACKAGE_NAME
import one.yufz.xposed.HookCallback
import one.yufz.xposed.hook
import one.yufz.xposed.hookMethod

object NmsPermissionHooker {
    private fun fromHms() = try {
        Binder.getCallingUid() == getPackageUid(HMS_PACKAGE_NAME)
    } catch (e: Throwable) {
        false
    }

    private fun getPackageUid(packageName: String) = getContext().packageManager.getPackageUid(packageName, 0)

    private fun getContext(): Context = AndroidAppHelper.currentApplication()

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
        findMethodExact(classINotificationManager, "areNotificationsEnabledForPackage", String::class.java, Int::class.java)
            .hook(hookPermission(0))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, String conversationId, boolean includeDeleted);
            findMethodExact(classINotificationManager, "getNotificationChannelForPackage", String::class.java, Int::class.java, String::class.java, String::class.java, Boolean::class.java)
                .hook(hookPermission(0))
        } else {
            //NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted);
            findMethodExact(classINotificationManager, "getNotificationChannelForPackage", String::class.java, Int::class.java, String::class.java, Boolean::class.java)
                .hook(hookPermission(0))
        }

        //void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int userId)
        findMethodExact(classINotificationManager, "enqueueNotificationWithTag", String::class.java, String::class.java, String::class.java, Int::class.java, Notification::class.java, Int::class.java)
            .hook(hookPermission(0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    args[1] = ANDROID_PACKAGE_NAME
                }
            })

        //void createNotificationChannelsForPackage(String pkg, int uid, in ParceledListSlice channelsList);
        findMethodExact(classINotificationManager, "createNotificationChannelsForPackage", String::class.java, Int::class.java, findClass("android.content.pm.ParceledListSlice", null))
            .hook(hookPermission(0))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //void cancelNotificationWithTag(String pkg, String opPkg, String tag, int id, int userId);
            findMethodExact(classINotificationManager, "cancelNotificationWithTag", String::class.java, String::class.java, String::class.java, Int::class.java, Int::class.java)
                .hook(hookPermission(0) {
                    args[1] = ANDROID_PACKAGE_NAME
                })
        } else {
            //void cancelNotificationWithTag(String pkg, String opPkg, String tag, int id, int userId);
            findMethodExact(classINotificationManager, "cancelNotificationWithTag", String::class.java, String::class.java, Int::class.java, Int::class.java)
                .hook(hookPermission(0))
        }

        //void deleteNotificationChannel(String pkg, String channelId);
        findMethodExact(classINotificationManager, "deleteNotificationChannel", String::class.java, String::class.java)
            .hook(hookPermission(0))

        //ParceledListSlice getAppActiveNotifications(String callingPkg, int userId);
        findMethodExact(classINotificationManager, "getAppActiveNotifications", String::class.java, Int::class.java)
            .hook(hookPermission(0))

        //ParceledListSlice getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted);
        findMethodExact(classINotificationManager, "getNotificationChannelsForPackage", String::class.java, Int::class.java, Boolean::class.java)
            .hook(hookPermission(0))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            findClass("com.android.server.notification.PreferencesHelper", classINotificationManager.classLoader)
                //public boolean deleteNotificationChannel(String pkg, int uid, String channelId)
                .hookMethod("deleteNotificationChannel", String::class.java, Int::class.java, String::class.java) {
                    doBefore {
                        val packageName = args[0] as String
                        if (packageName != HMS_PACKAGE_NAME && Binder.getCallingUid() == Process.SYSTEM_UID) {
                            args[1] = getPackageUid(packageName)
                        }
                    }
                }
        } else {
            findClass("com.android.server.notification.RankingHelper", classINotificationManager.classLoader)
                //public void deleteNotificationChannel(String pkg, int uid, String channelId)
                .hookMethod("deleteNotificationChannel", String::class.java, Int::class.java, String::class.java) {
                    doBefore {
                        val packageName = args[0] as String
                        if (packageName != HMS_PACKAGE_NAME && Binder.getCallingUid() == Process.SYSTEM_UID) {
                            args[1] = getPackageUid(packageName)
                        }
                    }
                }
        }
    }
}