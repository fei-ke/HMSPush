package one.yufz.hmspush.hook.fakedevice

import android.content.pm.PackageInfo
import android.util.Base64
import dalvik.system.DexClassLoader
import one.yufz.hmspush.common.HMS_CORE_SIGNATURE
import one.yufz.hmspush.common.HMS_PACKAGE_NAME
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.*

object FakeHmsSignature {
    private const val TAG = "FakeHmsSignature"

    private var verifyApkHashHooked = false
    private var verifyApkHashUnhook: Unhook? = null

    fun hook(lpparam: LoadPackageParam) {
        XLog.d(TAG, "hook() called with: processName = ${lpparam.processName}")

        tryHookVerifyApkHash(lpparam.classLoader)

        if (!verifyApkHashHooked) {
            verifyApkHashUnhook = DexClassLoader::class.java.hookConstructor(String::class.java, String::class.java, String::class.java, ClassLoader::class.java) {
                doAfter { tryHookVerifyApkHash(thisObject as ClassLoader) }
            }
        }

        val classApplicationPackageManager = lpparam.classLoader.findClass("android.app.ApplicationPackageManager")
        classApplicationPackageManager.hookMethod("getPackageInfo", String::class.java, Int::class.java) {
            doAfter {
                val packageName = args[0] as String
                if (packageName == HMS_PACKAGE_NAME) {
                    val info = result as PackageInfo
                    info.signatures?.firstOrNull()?.let {
                        info.signatures[0]["mSignature"] = Base64.decode(HMS_CORE_SIGNATURE, Base64.NO_WRAP)
                    }
                }
            }
        }
    }

    private fun tryHookVerifyApkHash(classLoader: ClassLoader) {
        if (verifyApkHashHooked) return

        try {
            classLoader.findClass("com.huawei.hms.utils.ReadApkFileUtil")
                .hookMethod("verifyApkHash", String::class.java) { replace { true } }

            XLog.d(TAG, "tryHookVerifyApkHash: verifyApkHash() hooked")

            verifyApkHashHooked = true
            verifyApkHashUnhook?.unhook()
        } catch (e: ClassNotFoundException) {
            XLog.d(TAG, "tryHookVerifyApkHash: ClassNotFoundException")
        } catch (e: NoSuchMethodError) {
            XLog.d(TAG, "tryHookVerifyApkHash: NoSuchMethodError")
        } catch (e: Throwable) {
            //ignore
        }
    }
}