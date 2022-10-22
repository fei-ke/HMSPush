package one.yufz.hmspush.hook.fakedevice

import android.app.Application
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.xposed.hookMethod

class CoolApk : XGPush() {
    override fun fake(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        Application::class.java.hookMethod("onCreate") {
            doAfter { super.fake(lpparam) }
        }
        return true
    }
}