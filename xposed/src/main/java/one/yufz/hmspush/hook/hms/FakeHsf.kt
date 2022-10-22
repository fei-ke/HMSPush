package one.yufz.hmspush.hook.hms

import android.content.Context
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.findClass
import one.yufz.xposed.hookAllMethods
import one.yufz.xposed.hookMethod

object FakeHsf {
    private const val TAG = "FakeHsf"

    fun hook(classLoader: ClassLoader) {
        XLog.d(TAG, "hook() called with: classLoader = $classLoader")

        classLoader.findClass("com.huawei.hsf.common.api.HsfAvailability").hookMethod("getInstance") {
            doAfter { hookHsfAvailabilityImpl(result.javaClass) }
        }

        classLoader.findClass("com.huawei.hsf.common.api.HsfApi").hookAllMethods("newInstance") {
            doAfter { hookHsfApiImpl(result.javaClass) }
        }
    }

    private fun hookHsfAvailabilityImpl(classHsfAvailabilityImpl: Class<*>) {
        XLog.d(TAG, "hookHsfAvailabilityImpl() called with: classHsfAvailabilityImpl = $classHsfAvailabilityImpl")

        //int isHuaweiMobileServicesAvailable(Context context);
        classHsfAvailabilityImpl.hookMethod("isHuaweiMobileServicesAvailable", Context::class.java) {
            replace { 0 }
        }
    }

    private fun hookHsfApiImpl(classHsfApiImpl: Class<*>) {
        XLog.d(TAG, "hookHsfApiImpl() called with: classHsfApiImpl = $classHsfApiImpl")

        classHsfApiImpl.hookMethod("isConnected") { replace { true } }
    }
}