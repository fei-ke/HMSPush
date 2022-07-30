package one.yufz.hmspush.fakedevice

import android.os.Build
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.HookContext
import one.yufz.hmspush.findClass
import one.yufz.hmspush.hookMethod
import one.yufz.hmspush.set

class Common : IFakeDevice {
    override fun fake(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        val classSystemProperties = lpparam.classLoader.findClass("android.os.SystemProperties")
        val callback: HookContext.() -> Unit = {
            doBefore {
                val key = args[0] as String
                when (key) {
                    "ro.build.hw_emui_api_level" -> result = "21"
                    "ro.build.version.emui" -> result = "EmotionUI"
                    "ro.product.brand", "ro.product.manufacturer" -> result = "HUAWEI"
                    "ro.miui.ui.version.name" -> result = ""
                }
            }
        }
        classSystemProperties.hookMethod("get", String::class.java, callback = callback)
        classSystemProperties.hookMethod("get", String::class.java, String::class.java, callback = callback)

        Build::class.java["BRAND"] = "HUAWEI"
        Build::class.java["MANUFACTURER"] = "HUAWEI"

        return true
    }
}