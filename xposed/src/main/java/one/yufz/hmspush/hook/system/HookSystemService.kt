package one.yufz.hmspush.hook.system

import android.app.AndroidAppHelper
import android.app.NotificationManager
import android.content.Context
import android.os.Binder
import android.os.Build
import de.robv.android.xposed.XposedHelpers
import one.yufz.hmspush.common.ANDROID_PACKAGE_NAME
import one.yufz.hmspush.common.IS_SYSTEM_HOOK_READY
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.callMethod
import one.yufz.xposed.deoptimizeMethod
import one.yufz.xposed.get
import one.yufz.xposed.hook
import one.yufz.xposed.hookMethod

class HookSystemService {
    companion object {
        private const val TAG = "HookSystemService"

        val isSystemHookReady: Boolean by lazy {
            try {
                val nm = AndroidAppHelper.currentApplication().getSystemService(NotificationManager::class.java)
                nm.callMethod("isSystemConditionProviderEnabled", IS_SYSTEM_HOOK_READY) as Boolean
            } catch (t: Throwable) {
                XLog.e(TAG, "isSystemHookReady error", t)
                false
            }
        }

    }

    fun hook(classLoader: ClassLoader) {
        val classNotificationManagerService = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService", classLoader)

        classNotificationManagerService.hookMethod("onStart") {
            doAfter {
                val context = thisObject.callMethod("getContext") as Context
                KeepHmsAlive(context).start()
                val stubClass = thisObject.get<Any>("mService").javaClass
                hookPermission(stubClass)
                hookSystemReadyFlag(stubClass)
            }
        }

        //private boolean isPackageSuspendedForUser(String pkg, int uid)
        classNotificationManagerService.hookMethod("isPackageSuspendedForUser", String::class.java, Int::class.java) {
            doBefore {
                if (Binder.getCallingUid() == 1000) {
                    //suspend app can not show notification, fake its state
                    result = false
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //int resolveNotificationUid(String callingPkg, String targetPkg, int callingUid, int userId)
            XposedHelpers.findMethodExact(classNotificationManagerService, "resolveNotificationUid", String::class.java, String::class.java, Int::class.java, Int::class.java)
                .deoptimizeMethod()

            //https://cs.android.com/android/platform/superproject/+/android-cts-10.0_r1:frameworks/base/services/core/java/com/android/server/notification/NotificationManagerService.java;drc=86869c922207a240884697215ba0bf5b89bd0b37;l=1738
            // there is a bug from android 10, the enqueueNotificationInternal method 3rd parameter is need a callingUid, in this method, r.sbn.getUid() actually is the targetUid
            // when a notification post from HMSPush and snoozed, then the notification will never show again
            // this hook temporary fix this issue
            try {
                classNotificationManagerService.hookMethod("isCallerAndroid", String::class.java, Int::class.java) {
                    doBefore {
                        val callingPkg = args[0] as String
                        if (callingPkg == ANDROID_PACKAGE_NAME) {
                            result = true
                        }
                    }
                }
            } catch (e: Throwable) {
                //Samsung One UI 7 delete this method
                XLog.e(TAG, "hook isCallerAndroid error", e)
            }
        }

        val classShortcutService = XposedHelpers.findClass("com.android.server.pm.ShortcutService", classLoader)
        ShortcutPermissionHooker.hook(classShortcutService)
    }

    private fun hookSystemReadyFlag(stubClass: Class<Any>) {
        stubClass.hookMethod("isSystemConditionProviderEnabled", String::class.java) {
            doBefore {
                if (args[0] == IS_SYSTEM_HOOK_READY) {
                    result = true
                }
            }
        }
    }

    private fun hookPermission(stubClass: Class<Any>) {
        NmsPermissionHooker.hook(stubClass)
    }
}