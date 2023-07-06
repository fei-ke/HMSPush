package one.yufz.hmspush.hook.fakedevice

import android.content.ContentResolver
import android.provider.Settings
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.hookMethod

object HookHmsDeviceId {
    private const val TAG = "HookHmsDeviceId"

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        XLog.d(TAG, "hook() called with: processName = ${lpparam.processName}")

        Settings.Global::class.java.hookMethod("getString", ContentResolver::class.java, String::class.java) {
            doBefore {
                if (args[1] == "pps_oaid") {
                    result = "00000000-0000-0000-0000-000000000000"
                } else if (args[1] == "pps_track_limit") {
                    result = "true"
                }
            }
        }
    }
}