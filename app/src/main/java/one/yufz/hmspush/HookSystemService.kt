package one.yufz.hmspush

import android.content.Context
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Parcel
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
                    hookPermission(context)
                }
            }
        }

        hookPackageManager(classLoader)
    }

    private fun hookPackageManager(classLoader: ClassLoader) {
        classLoader.findClass("com.android.server.pm.PackageManagerService").hookMethod("getInstallerPackageName", String::class.java) {
            doBefore {
                if (args[0] == IS_SYSTEM_HOOK_READY) {
                    result = READY
                }
            }
        }
    }

    private fun hookPermission(context: Context) {
        XLog.d(TAG, "hookPermission() called")

        fun isHms() = try {
            Binder.getCallingUid() == context.packageManager.getPackageUid(HMS_PACKAGE_NAME, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            XLog.d(TAG, "isHms() called, NameNotFoundException caught")
            false
        }

        XposedHelpers.findClass("android.app.INotificationManager\$Stub", null)
            .hookMethod("onTransact", Int::class.java, Parcel::class.java, Parcel::class.java, Int::class.java) {
                var token = 0L
                doBefore {
                    if (isHms()) {
                        token = Binder.clearCallingIdentity()
                    }
                }

                doAfter {
                    if (token != 0L) {
                        Binder.restoreCallingIdentity(token)
                    }
                }
            }
    }
}