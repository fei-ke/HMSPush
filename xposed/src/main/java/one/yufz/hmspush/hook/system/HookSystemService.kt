package one.yufz.hmspush.hook.system

import android.app.AndroidAppHelper
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Binder
import android.os.Build
import android.os.Process
import de.robv.android.xposed.XposedHelpers
import one.yufz.hmspush.common.IS_SYSTEM_HOOK_READY
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.HookContext
import one.yufz.xposed.callMethod
import one.yufz.xposed.get
import one.yufz.xposed.hookAllMethods
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

        hookCreateNotificationChannel(classLoader)
    }

    private fun hookCreateNotificationChannel(classLoader: ClassLoader) {
        val callback: HookContext.() -> Unit = {
            doBefore {
                val channel = args[2] as NotificationChannel
                val callingUid = Binder.getCallingUid()
                if (callingUid == Process.SYSTEM_UID && channel.id.startsWith("com.huawei.hms.pushagent")) {
                    XLog.i(TAG, "createNotificationChannel: seem from hms, change fromTargetApp to false")
                    args[3] = false
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //    public boolean createNotificationChannel(String pkg, int uid, NotificationChannel channel, boolean fromTargetApp, boolean hasDndAccess,
            //    int callingUid, boolean fromSystemOrSystemUi)
            XposedHelpers.findClass("com.android.server.notification.PreferencesHelper", classLoader)
                .hookAllMethods("createNotificationChannel", callback)
        } else {
            //public void createNotificationChannel(String pkg, int uid, NotificationChannel channel, boolean fromTargetApp, boolean hasDndAccess)
            XposedHelpers.findClass("com.android.server.notification.RankingHelper", classLoader)
                .hookAllMethods("createNotificationChannel", callback)
        }
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