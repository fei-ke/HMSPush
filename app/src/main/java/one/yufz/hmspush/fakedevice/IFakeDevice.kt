package one.yufz.hmspush.fakedevice

import de.robv.android.xposed.callbacks.XC_LoadPackage

interface IFakeDevice {
    fun fake(lpparam: XC_LoadPackage.LoadPackageParam): Boolean
}