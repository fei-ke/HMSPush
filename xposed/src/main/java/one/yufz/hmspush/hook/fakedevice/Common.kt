package one.yufz.hmspush.hook.fakedevice

import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.hook.XLog

open class Common : IFakeDevice {
    companion object {
        private const val TAG = "Common"
    }

    override fun fake(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        XLog.d(TAG, "fake() called with: packageName = ${lpparam.packageName}")
        fakeAllBuildInProperties()
        return true
    }
}