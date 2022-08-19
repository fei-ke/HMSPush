package one.yufz.hmspush

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import one.yufz.hmspush.fakedevice.FakeDevice

class XposedMod : IXposedHookLoadPackage {
    companion object {
        private const val TAG = "XposedMod"
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
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

        FakeDevice.fake(lpparam)
    }
}