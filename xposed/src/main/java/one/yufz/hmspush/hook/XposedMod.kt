package one.yufz.hmspush.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.common.doOnce
import one.yufz.xposed.LoadPackageParam
import one.yufz.xposed.toLegacyLoadPackageParam

class XposedMod : IXposedHookLoadPackage {
    companion object {
        private const val TAG = "XposedMod"
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        doOnce(lpparam.classLoader) {
            hook(lpparam.toLegacyLoadPackageParam())
        }
    }

    private fun hook(lpparam: LoadPackageParam) {
        XLog.d(TAG, "Loaded app: " + lpparam.packageName + " process:" + lpparam.processName)
        HookEntry.onHook(lpparam)
    }
}