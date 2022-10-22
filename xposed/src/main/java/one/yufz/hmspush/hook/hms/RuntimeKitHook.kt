package one.yufz.hmspush.hook.hms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Handler
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.findClass
import one.yufz.xposed.hookMethod
import java.util.*

object RuntimeKitHook {
    private const val TAG = "RuntimeKitHook"

    private val receivers: MutableMap<BroadcastReceiver, Context> = WeakHashMap()

    fun hook(classLoader: ClassLoader) {
        classLoader.findClass("com.huawei.hms.runtimekit.container.kitsdk.KitContext")
            .hookMethod("registerReceiver", BroadcastReceiver::class.java, IntentFilter::class.java, String::class.java, Handler::class.java) {
                doAfter {
                    val receiver = args[0] as BroadcastReceiver
                    val intentFilter = args[1] as IntentFilter
                    if (intentFilter.hasAction("android.intent.action.PACKAGE_REMOVED")
                        && intentFilter.hasAction("android.intent.action.PACKAGE_DATA_CLEARED")
                        && intentFilter.hasDataScheme("package")
                    ) {
                        receivers[receiver] = thisObject as Context
                        XLog.d(TAG, "receiver added: $receiver")
                    }
                }
            }
    }

    fun sendFakePackageRemoveBroadcast(packageName: String) {
        XLog.d(TAG, "sendFakePackageRemoveBroadcast() called with: packageName = $packageName")

        val intent = Intent("android.intent.action.PACKAGE_REMOVED").apply {
            data = Uri.parse("package:${packageName}")
        }

        receivers.forEach { (receiver, context) ->

            receiver.onReceive(context, intent)

            XLog.d(TAG, "sendFakePackageRemoveBroadcast() send to: $receiver")
        }
    }
}