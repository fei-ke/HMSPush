package one.yufz.hmspush.fakedevice

import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.XLog

object FakeDevice {
    private const val TAG = "FakeDevice"

    private val Default = arrayOf(Common::class.java)
    private val FakeDeviceConfig: Map<String, Array<Class<out IFakeDevice>>> = mapOf(
        "com.coolapk.market" to arrayOf(CoolApk::class.java),
    )

    fun fake(lpparam: XC_LoadPackage.LoadPackageParam) {
        XLog.d(TAG, "fake() called with: packageName = ${lpparam.packageName}, processName = ${lpparam.processName}")
        val fakes = FakeDeviceConfig[lpparam.packageName] ?: Default
        fakes.forEach { it.newInstance().fake(lpparam) }
    }
}