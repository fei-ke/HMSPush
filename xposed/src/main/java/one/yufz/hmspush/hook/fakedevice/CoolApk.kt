package one.yufz.hmspush.hook.fakedevice

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.LoadPackageParam
import one.yufz.xposed.hookMethod

class CoolApk : XGPush() {
    companion object {
        private const val TAG = "CoolApk"
    }

    override fun fake(lpparam: LoadPackageParam): Boolean {
        Application::class.java.hookMethod("attach", Context::class.java) {
            doAfter {
                super.fake(lpparam)

                val context = thisObject as Context
                listOf(
                    "com.huawei.agconnect.core.provider.AGConnectInitializeProvider",
                    "com.huawei.agconnect.core.ServiceDiscovery",
                    "com.tencent.android.hwpushv3.HWHmsMessageService",
                    "com.huawei.hms.support.api.push.PushMsgReceiver",
                    "com.huawei.hms.support.api.push.PushReceiver",
                    "com.huawei.hms.support.api.push.PushProvider",
                )
                    .map { ComponentName(context, it) }
                    .filter { context.packageManager.getComponentEnabledSetting(it) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED }
                    .forEach {
                        try {
                            context.packageManager.setComponentEnabledSetting(it, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0)
                        } catch (t: Throwable) {
                            XLog.d(TAG, t.message)
                        }
                    }
            }
        }
        return true
    }
}