package one.yufz.hmspush

import android.os.Build
import com.huawei.android.app.NotificationManagerEx
import dalvik.system.DexClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class XposedMod : IXposedHookLoadPackage {
    companion object {
        private const val TAG = "XposedMod"
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        XLog.d(TAG, "Loaded app: " + lpparam.packageName + " process:" + lpparam.processName)

        if (lpparam.packageName == ANDROID_PACKAGE_NAME) {
            HookSystemService().hook(lpparam.classLoader)
            return
        }

        if (lpparam.packageName == HMS_PACKAGE_NAME) {
            if (lpparam.processName == HMS_CORE_PROCESS) {
                hookHMS()
            }
            return
        }

        fakeDevice(lpparam)
    }

    private fun hookHMS() {
        DexClassLoader::class.java.hookAllConstructor {
            doAfter {
                val dexPath = args[0] as String
                if (dexPath.contains("push")) {
                    XLog.d(TAG, "load push related dex path: $dexPath")

                    val paths = dexPath.split("/")
                    val version = paths.getOrNull(paths.size - 2)?.toIntOrNull() ?: 0

                    XLog.d(TAG, "load push version: $version")

                    val classLoader = thisObject as ClassLoader

                    if (dexPath.endsWith(HMS_PUSH_NC)) {
                        if (version >= 60600300) {
                            HookPushNC.hook(classLoader)
                        } else {
                            hookLegacyPush(classLoader)
                        }
                    } else if (version <= 60300301) {
                        hookLegacyPush(classLoader)
                    }
                }
            }
        }
    }

    private fun fakeDevice(lpparam: LoadPackageParam) {
        val classSystemProperties = lpparam.classLoader.findClass("android.os.SystemProperties")
        val callback: HookContext.() -> Unit = {
            doBefore {
                val key = args[0] as String
                when (key) {
                    "ro.build.hw_emui_api_level" -> result = "21"
                    //"ro.product.model" -> result = "NOP-AN00"
                    "ro.product.brand", "ro.product.manufacturer" -> result = "HUAWEI"
                    "ro.miui.ui.version.name" -> result = ""
                }
            }
        }
        classSystemProperties.hookMethod("get", String::class.java, callback = callback)
        classSystemProperties.hookMethod("get", String::class.java, String::class.java, callback = callback)

        //Build::class.java["MODEL"] = "NOP-AN00"
        Build::class.java["BRAND"] = "HUAWEI"
        Build::class.java["MANUFACTURER"] = "HUAWEI"
    }

    private fun hookLegacyPush(classLoader: ClassLoader) {
        XLog.d(TAG, "hookLegacyPush() called with: classLoader = $classLoader")

        PushSignWatcher().watch()

        Class::class.java.hookMethod("forName", String::class.java, Boolean::class.java, ClassLoader::class.java) {
            doBefore {
                if (args[0] == NotificationManagerEx::class.java.name) {
                    result = NotificationManagerEx::class.java
                }
            }
        }
    }
}