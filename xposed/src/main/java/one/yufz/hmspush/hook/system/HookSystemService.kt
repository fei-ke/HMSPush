package one.yufz.hmspush.hook.system

import android.app.AndroidAppHelper
import android.app.NotificationManager
import android.content.Context
import android.os.Binder
import de.robv.android.xposed.XposedHelpers
import one.yufz.hmspush.common.IS_SYSTEM_HOOK_READY
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.callMethod
import one.yufz.xposed.get
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