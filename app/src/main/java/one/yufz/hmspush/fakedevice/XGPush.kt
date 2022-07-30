package one.yufz.hmspush.fakedevice

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.XLog
import one.yufz.hmspush.findClass
import java.lang.reflect.Method

open class XGPush : IFakeDevice {
    companion object {
        private const val TAG = "FakeForXGPush"
    }

    override fun fake(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        val classLoader = lpparam.classLoader

        XLog.d(TAG, "fake() called with: classLoader = $classLoader")

        return try {
            val classChannelUtils = classLoader.findClass("com.tencent.tpns.baseapi.base.util.ChannelUtils")
            fakeChannels(classChannelUtils)
            true
        } catch (e: XposedHelpers.ClassNotFoundError) {
            XLog.e(TAG, "fake ClassNotFoundError", e)
            false
        } catch (e: Throwable) {
            XLog.e(TAG, "fake error: ", e)
            false
        }
    }

    private fun fakeChannels(classChannelUtils: Class<*>): Boolean {
        XLog.d(TAG, "fakeChannels() called")

        classChannelUtils.declaredMethods.forEach {
            XposedBridge.hookMethod(it, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val method = param.method as Method
                    if (method.name == "isBrandHuaWei") {
                        param.result = true
                    } else if (method.returnType == Boolean::class.java) {
                        param.result = false
                    } else if (method.returnType == String::class.java) {
                        param.result = ""
                    }
                }
            })
        }
        return true
    }


}