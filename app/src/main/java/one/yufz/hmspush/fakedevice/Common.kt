package one.yufz.hmspush.fakedevice

import android.os.Build
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.*

class Common : IFakeDevice {
    companion object {
        private const val TAG = "Common"

        private val FAKE_PROPERTIES = mapOf(
            "ro.build.hw_emui_api_level" to "21",
            "ro.build.version.emui" to "EmotionUI",
            "ro.product.brand" to "HUAWEI",
            "ro.product.manufacturer" to "HUAWEI",
            "ro.miui.ui.version.name" to "",
        )
    }

    override fun fake(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        val classSystemProperties = lpparam.classLoader.findClass("android.os.SystemProperties")
        val callback: HookContext.() -> Unit = {
            doBefore {
                val key = args[0] as String
                FAKE_PROPERTIES[key]?.let {
                    result = it
                }
            }
        }
        classSystemProperties.hookMethod("get", String::class.java, callback = callback)
        classSystemProperties.hookMethod("get", String::class.java, String::class.java, callback = callback)

        Build::class.java["BRAND"] = "HUAWEI"
        Build::class.java["MANUFACTURER"] = "HUAWEI"

        Runtime::class.java.hookMethod("exec", String::class.java) {
            doBefore {
                val cmd = args[0] as String
                if (cmd.startsWith("getprop")) {
                    val key = cmd.removePrefix("getprop").trim()
                    FAKE_PROPERTIES[key]?.let {
                        XLog.d(TAG, "hook getprop $key")
                        args[0] = "echo $it"
                    }
                }
            }
        }

        return true
    }
}