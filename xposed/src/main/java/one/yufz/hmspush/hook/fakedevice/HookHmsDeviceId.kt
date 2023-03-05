package one.yufz.hmspush.hook.fakedevice

import android.content.Context
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.findClass
import one.yufz.xposed.hookMethod
import one.yufz.xposed.onDexClassLoaderLoaded

object HookHmsDeviceId {
    private const val TAG = "HookHmsDeviceId"

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        XLog.d(TAG, "hook() called with: processName = ${lpparam.processName}")

        if (tryHookOaid(lpparam.classLoader)) {
            return
        }

        onDexClassLoaderLoaded {
            if (tryHookOaid(this)) {
                it.invoke()
            }
        }
    }

    private fun tryHookOaid(classLoader: ClassLoader): Boolean {
        return try {
            classLoader.findClass("com.huawei.hms.ads.identifier.AdvertisingIdClient")
                .hookMethod("getAdvertisingIdInfo", Context::class.java) {
                    replace {
                        val info = constructIdInfo(classLoader)
                        XLog.d(TAG, "getAdvertisingIdInfo() called, returned")
                        return@replace info
                    }
                }
            XLog.d(TAG, "tryHookOaid() called AdvertisingIdClient hooked")
            true
        } catch (t: ClassNotFoundError) {
            false
        } catch (t: Throwable) {
            XLog.e(TAG, "hook AdvertisingIdClient.getAdvertisingIdInfo() error", t)
            false
        }
    }

    private fun constructIdInfo(classLoader: ClassLoader) = try {
        classLoader.findClass("com.huawei.hms.ads.identifier.AdvertisingIdClient\$Info")
            .getConstructor(String::class.java, Boolean::class.java)
            .newInstance("00000000-0000-0000-0000-000000000000", true)
    } catch (e: Exception) {
        null
    }
}