package one.yufz.hmspush.hook.fakedevice

import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.LoadPackageParam

open class Common : IFakeDevice {
    companion object {
        private const val TAG = "Common"
    }

    override fun fake(lpparam: LoadPackageParam): Boolean {
        XLog.d(TAG, "fake() called with: packageName = ${lpparam.packageName}")
        fakeAllBuildInProperties()
        return true
    }
}