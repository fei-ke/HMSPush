package one.yufz.hmspush.hook.fakedevice

import one.yufz.xposed.LoadPackageParam


interface IFakeDevice {
    fun fake(lpparam: LoadPackageParam): Boolean
}