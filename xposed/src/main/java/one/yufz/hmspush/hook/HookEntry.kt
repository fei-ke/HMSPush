package one.yufz.hmspush.hook

import one.yufz.hmspush.common.ANDROID_PACKAGE_NAME
import one.yufz.hmspush.common.HMS_CORE_PROCESS
import one.yufz.hmspush.common.HMS_PACKAGE_NAME
import one.yufz.hmspush.hook.fakedevice.FakeDevice
import one.yufz.hmspush.hook.hms.HookHMS
import one.yufz.hmspush.hook.system.HookSystemService
import one.yufz.xposed.LoadPackageParam

object HookEntry {
    fun onHook(lpparam: LoadPackageParam) {

        if (lpparam.processName == ANDROID_PACKAGE_NAME) {
            if (lpparam.packageName == ANDROID_PACKAGE_NAME) {
                HookSystemService().hook(lpparam.classLoader)
            }
            return
        }

        if (lpparam.packageName == HMS_PACKAGE_NAME) {
            if (lpparam.processName == HMS_CORE_PROCESS) {
                HookHMS().hook(lpparam.classLoader)
            }
            return
        }

        if (lpparam.packageName == "com.android.systemui") {
            return
        }

        FakeDevice.fake(lpparam)
    }
}