package one.yufz.hmspush.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import one.yufz.hmspush.common.ANDROID_PACKAGE_NAME
import one.yufz.hmspush.common.HMS_CORE_PROCESS
import one.yufz.hmspush.common.HMS_PACKAGE_NAME
import one.yufz.hmspush.common.doOnce
import one.yufz.hmspush.hook.fakedevice.FakeDevice
import one.yufz.hmspush.hook.hms.HookHMS
import one.yufz.hmspush.hook.system.HookSystemService

class XposedMod : IXposedHookLoadPackage {
    companion object {
        private const val TAG = "XposedMod"
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        doOnce(lpparam.classLoader) {
            hook(lpparam)
        }
    }

    private fun hook(lpparam: LoadPackageParam) {
        XLog.d(TAG, "Loaded app: " + lpparam.packageName + " process:" + lpparam.processName)

        if (lpparam.processName == ANDROID_PACKAGE_NAME) {
            if (lpparam.packageName == ANDROID_PACKAGE_NAME) {
                HookSystemService().hook(lpparam.classLoader)
            }
            return
        }

        if (lpparam.packageName == HMS_PACKAGE_NAME) {
            if (lpparam.processName == HMS_CORE_PROCESS) {
                HookHMS().hook(lpparam)
            }
            return
        }

        if (lpparam.packageName == "com.android.systemui") {
            return
        }

        FakeDevice.fake(lpparam)
    }
}