package one.yufz.hmspush

import android.content.ComponentName
import android.content.Context
import android.os.Binder
import android.os.Parcel
import de.robv.android.xposed.XposedHelpers

class HookSystemService() {
    fun hook(classLoader: ClassLoader) {
        val classNotificationManagerService = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService", classLoader)
        classNotificationManagerService.hookConstructor(Context::class.java) {
            doAfter {
                hookPermission(args[0] as Context)
            }
        }

        classNotificationManagerService.hookMethod("onBootPhase", Int::class.java) {
            doAfter {
                //com.android.server.SystemService#PHASE_BOOT_COMPLETED
                if (args[0] == 1000) {
                    KeepHmsAlive(thisObject.callMethod("getContext") as Context).start()
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

    private fun hookStub(stub: Any) {
        stub.javaClass.hookMethod("isNotificationListenerAccessGranted", ComponentName::class.java) {
            doBefore {
                val componentName = args[0] as ComponentName
                if (componentName.packageName == HMS_PACKAGE_NAME && componentName.className == IS_SYSTEM_HOOK_READY) {
                    result = true
                }
            }
        }
    }

    private fun hookPermission(context: Context) {
        fun isHms(): Boolean {
            val callingUid = Binder.getCallingUid()
            val hmsUid = context.packageManager.getPackageUid(HMS_PACKAGE_NAME, 0)
            return callingUid == hmsUid
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