package one.yufz.hmspush.hook.fakedevice

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.common.BridgeWrap
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.hookMethod

object FakeDevice {
    private const val TAG = "FakeDevice"

    private val Default = arrayOf(Common::class.java)
    private val FakeDeviceConfig: Map<String, Array<Class<out IFakeDevice>>> = mapOf(
        "com.coolapk.market" to arrayOf(CoolApk::class.java),
        "com.tencent.mobileqq" to arrayOf(QQ::class.java),
        "com.tencent.tim" to arrayOf(QQ::class.java),
        "com.sankuai.meituan" to arrayOf(FakeEmuiOnly::class.java),
        "com.sankuai.meituan.takeoutnew" to arrayOf(FakeEmuiOnly::class.java),
        "com.dianping.v1" to arrayOf(FakeEmuiOnly::class.java),
        "com.eg.android.AlipayGphone" to arrayOf(Alipay::class.java),
        "com.xunmeng.pinduoduo" to arrayOf(PinDuoDuo::class.java),
        "com.ss.android.ugc.aweme" to arrayOf(DouYin::class.java),
    )

    fun fake(lpparam: XC_LoadPackage.LoadPackageParam) {
        XLog.d(TAG, "fake() called with: packageName = ${lpparam.packageName}, processName = ${lpparam.processName}")
        if (lpparam.packageName == "com.google.android.webview") {
            XLog.d(TAG, "fake() called, ignore ${lpparam.packageName}")
            return
        }

        val fakes = FakeDeviceConfig[lpparam.packageName] ?: Default
        fakes.forEach { it.newInstance().fake(lpparam) }

        ContextWrapper::class.java.hookMethod("attachBaseContext", Context::class.java) {
            doAfter {
                XLog.d(TAG, "${thisObject}.attachBaseContext() called")
                try {
                    if (thisObject is Application) {
                        unhook()
                        if (BridgeWrap.isDisableSignature(thisObject as Context)) {
                            FakeHmsSignature.hook(lpparam)
                        }
                    }
                } catch (t: Throwable) {
                    XLog.e(TAG, "disable signature error", t)
                }
            }
        }
    }
}