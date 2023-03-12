package one.yufz.hmspush.hook.fakedevice

import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.LoadPackageParam
import one.yufz.xposed.findClass
import one.yufz.xposed.hook
import java.lang.reflect.Method

open class XGPush : IFakeDevice {
    companion object {
        private const val TAG = "FakeForXGPush"
    }

    override fun fake(lpparam: LoadPackageParam): Boolean {
        val classLoader = lpparam.classLoader

        XLog.d(TAG, "fake() called with: classLoader = $classLoader")

        return try {
            val classChannelUtils = classLoader.findClass("com.tencent.tpns.baseapi.base.util.ChannelUtils")
            fakeChannels(classChannelUtils)
            true
        } catch (e: ClassNotFoundException) {
            XLog.e(TAG, "fake ClassNotFoundException", e)
            false
        } catch (e: Throwable) {
            XLog.e(TAG, "fake error: ", e)
            false
        }
    }

    private fun fakeChannels(classChannelUtils: Class<*>): Boolean {
        XLog.d(TAG, "fakeChannels() called")

        classChannelUtils.declaredMethods.forEach {
            it.hook {
                doBefore {
                    val method = method as Method
                    if (method.name == "isBrandHuaWei") {
                        result = true
                    } else if (method.returnType == Boolean::class.java) {
                        result = false
                    } else if (method.returnType == String::class.java) {
                        result = ""
                    }
                }
            }
        }
        return true
    }
}