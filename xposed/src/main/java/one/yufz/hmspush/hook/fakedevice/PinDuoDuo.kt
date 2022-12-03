package one.yufz.hmspush.hook.fakedevice

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.xposed.hookMethod

class PinDuoDuo : Common() {
    companion object {
        private const val TAG = "PddCommon"
    }

    override fun fake(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        super.fake(lpparam)
        Application::class.java.hookMethod("attach", Context::class.java) {
            doAfter {
                val context: Context = thisObject as Context
                val hwPushReceiver = ComponentName(context, "com.aimi.android.common.push.huawei.HwPushReceiver")
                context.packageManager.setComponentEnabledSetting(hwPushReceiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0)
            }
        }
        return true
    }
}