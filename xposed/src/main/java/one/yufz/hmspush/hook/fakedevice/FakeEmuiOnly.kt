package one.yufz.hmspush.hook.fakedevice

import one.yufz.xposed.LoadPackageParam


class FakeEmuiOnly : IFakeDevice {
    override fun fake(lpparam: LoadPackageParam): Boolean {
        fakeProperty(Property.EMUI_VERSION)
        return true
    }
}