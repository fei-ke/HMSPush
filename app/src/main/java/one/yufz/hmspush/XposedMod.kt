package one.yufz.hmspush

import android.content.pm.PackageManager
import android.os.Build
import com.huawei.android.app.NotificationManagerEx
import dalvik.system.DexClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class XposedMod : IXposedHookLoadPackage {
    companion object {
        private const val TAG = "XposedMod.HMSPush"
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        XLog.d(TAG, "Loaded app: " + lpparam.packageName + " process:" + lpparam.processName)

        if (lpparam.packageName == "android") {
            hookSystemServer(lpparam.classLoader)
            return
        }

        if (lpparam.packageName == HMS_PACKAGE_NAME) {
            hookHMS()
            return
        }

        fakeDevices(lpparam)
    }

    private fun hookHMS() {
        DexClassLoader::class.java.hookAllConstructor {
            doAfter {
                val dexPath = args[0] as String
                if (dexPath.contains(HMS_PUSH_PACKAGE_NAME)) {
                    hookPush(thisObject as ClassLoader)
                }
            }
        }
    }

    private fun fakeDevices(lpparam: LoadPackageParam) {
        val classSystemProperties = lpparam.classLoader.findClass("android.os.SystemProperties")
        val callback: HookContext.() -> Unit = {
            doBefore {
                val key = args[0] as String
                when (key) {
                    "ro.build.hw_emui_api_level" -> result = "21"
                    //"ro.product.model" -> result = "NOP-AN00"
                    "ro.product.brand", "ro.product.manufacturer" -> result = "HUAWEI"
                }
            }
        }
        classSystemProperties.hookMethod("get", String::class.java, callback = callback)
        classSystemProperties.hookMethod("get", String::class.java, String::class.java, callback = callback)

        //Build::class.java["MODEL"] = "NOP-AN00"
        Build::class.java["BRAND"] = "HUAWEI"
        Build::class.java["MANUFACTURER"] = "HUAWEI"
    }

    private fun hookSystemServer(classLoader: ClassLoader) {
        val classPreferencesHelper = XposedHelpers.findClass("com.android.server.notification.PreferencesHelper", classLoader)
        classPreferencesHelper.hookMethod("isDelegateAllowed", String::class.java, Int::class.java, String::class.java, Int::class.java) {
            doBefore {
                val potentialDelegatePkg = args[2] as String
                if (potentialDelegatePkg == HMS_PACKAGE_NAME) {
                    result = true
                }
            }
        }

        val classNotificationManagerService = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService", classLoader)
        classNotificationManagerService.hookMethod("isUidSystemOrPhone", Int::class.java) {
            doBefore {
                val pm: PackageManager = thisObject["mPackageManagerClient"]
                val uid = args[0] as Int
                try {
                    val hmsUid = pm.getPackageUid(HMS_PACKAGE_NAME, 0)
                    if (hmsUid == uid) {
                        result = true
                    }
                } catch (e: Exception) {
                    //ignore
                }
            }
        }
    }

    private fun hookPush(classLoader: ClassLoader) {
        Class::class.java.hookMethod("forName", String::class.java, Boolean::class.java, ClassLoader::class.java) {
            doBefore {
                if (args[0] == NotificationManagerEx::class.java.name) {
                    result = NotificationManagerEx::class.java
                }
            }
        }
    }
}