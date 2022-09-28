package one.yufz.hmspush

import android.content.Context
import android.os.Binder
import de.robv.android.xposed.XposedHelpers

class HookSystemService {
    companion object {
        private const val TAG = "HookSystemService"
    }

    fun hook(classLoader: ClassLoader) {
        val classNotificationManagerService = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService", classLoader)

        classNotificationManagerService.hookMethod("onBootPhase", Int::class.java) {
            doAfter {
                //com.android.server.SystemService#PHASE_BOOT_COMPLETED
                if (args[0] == 1000) {
                    val context = thisObject.callMethod("getContext") as Context
                    KeepHmsAlive(context).start()
                    val stubClass = thisObject.get<Any>("mService").javaClass
                    hookPermission(stubClass)
                    hookSystemReadyFlag(stubClass)
                }
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