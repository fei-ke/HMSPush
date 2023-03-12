package one.yufz.hmspush.hook.fakedevice

import one.yufz.xposed.LoadPackageParam

class QQ : Common() {

    override fun fake(lpparam: LoadPackageParam): Boolean {
        if (lpparam.packageName == lpparam.processName || lpparam.processName.endsWith(":MSF")) {
            return super.fake(lpparam)
        }
        return false
    }
}