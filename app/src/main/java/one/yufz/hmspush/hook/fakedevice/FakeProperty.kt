package one.yufz.hmspush.hook.fakedevice

import android.os.Build
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.*

private const val TAG = "FakeProperties"

enum class Property(val entry: Pair<String, String>) {
    EMUI_API("ro.build.hw_emui_api_level" to "21"),
    EMUI_VERSION("ro.build.version.emui" to "EmotionUI_8.0.0"),
    BRAND("ro.product.brand" to "HUAWEI"),
    MANUFACTURER("ro.product.manufacturer" to "HUAWEI"),
    MIUI_VERSION("ro.miui.ui.version.name" to "");

    val key: String
        get() = entry.first

    val value: String
        get() = entry.second
}


fun fakeProperty(property: Property, overrideValue: String) = fakeProperties(Pair(property.key, overrideValue))

fun fakeAllBuildInProperties() = fakeProperties(*Property.values().map { it.entry }.toTypedArray())

fun fakeProperties(vararg properties: Property) {
    fakeProperties(*properties.map { it.entry }.toTypedArray())
}

fun fakeProperties(vararg properties: Pair<String, String>) {
    val classSystemProperties = Build::class.java.classLoader.findClass("android.os.SystemProperties")

    val propertiesMap: Map<String, String> = mapOf(*properties)

    val callback: HookContext.() -> Unit = {
        doBefore {
            val key = args[0] as String
            propertiesMap[key]?.let {
                result = it
            }
        }
    }

    classSystemProperties.hookMethod("get", String::class.java, callback = callback)
    classSystemProperties.hookMethod("get", String::class.java, String::class.java, callback = callback)

    if (propertiesMap.containsKey(Property.BRAND.key)) {
        Build::class.java["BRAND"] = Property.BRAND.value
    }

    if (propertiesMap.containsKey(Property.MANUFACTURER.key)) {
        Build::class.java["MANUFACTURER"] = Property.MANUFACTURER.value
    }

    Runtime::class.java.hookMethod("exec", String::class.java) {
        doBefore {
            val cmd = args[0] as String
            if (cmd.startsWith("getprop")) {
                val key = cmd.removePrefix("getprop").trim()
                propertiesMap[key]?.let {
                    XLog.d(TAG, "hook getprop $key")
                    args[0] = "echo $it"
                }
            }
        }
    }
}