package one.yufz.hmspush.hook.hms

import android.content.Context
import android.content.Intent
import de.robv.android.xposed.XposedHelpers
import one.yufz.hmspush.common.HMS_CORE_PUSH_ACTION_REGISTRATION
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.callMethod
import one.yufz.xposed.findClass
import one.yufz.xposed.get
import one.yufz.xposed.hook
import one.yufz.xposed.hookMethod

object HookLegacyTokenRequest {
    private const val TAG = "HookLegacyTokenRequest"

    fun hook(classLoader: ClassLoader) {
        val classKmsMessageCenter = try {
            classLoader.findClass("com.huawei.hms.fwkit.message.KmsMessageCenter")
        } catch (e: Throwable) {
            null
        }
        XLog.d(TAG, "hook() called with: classKmsMessageCenter = ${classKmsMessageCenter?.classLoader}")

        classKmsMessageCenter?.hookMethod("register", String::class.java, Class::class.java, Boolean::class.java, Boolean::class.java) {
            doBefore {
                val uri = args[0] as String
                if (uri == "push.gettoken") {
                    hookGetTokenProcess(args[1] as Class<*>)
                }
            }
        }
    }

    private fun hookGetTokenProcess(clazz: Class<*>) {
        XLog.d(TAG, "hookGetTokenProcess() called with: clazz = $clazz")
        val classLoader = clazz.classLoader
        val classIMessageEntity = classLoader.findClass("com.huawei.hms.support.api.transport.IMessageEntity")
        val classTokenResp = classLoader.findClass("com.huawei.hms.support.api.entity.push.TokenResp")

        arrayOf(
            *XposedHelpers.findMethodsByExactParameters(clazz.superclass, Void.TYPE, classIMessageEntity, Int::class.java),
            *XposedHelpers.findMethodsByExactParameters(clazz.superclass, Void.TYPE, classIMessageEntity, Class::class.java, Int::class.java)
        ).forEach { method ->
            XLog.d(TAG, "hookGetTokenProcess() called with: method = $method")

            method.hook {
                doAfter {
                    if (args[0].javaClass == classTokenResp) {
                        mockReceive(thisObject, args[0])
                    }
                }
            }
        }
    }

    private fun mockReceive(process: Any, response: Any) {
        XLog.d(TAG, "mockReceive() called")

        val context: Context = process["context"]
        val packageName = process.get<Any>("clientIdentity").callMethod("getPackageName") as String
        val token: String = response["token"]
        val intent = Intent(HMS_CORE_PUSH_ACTION_REGISTRATION)
        intent.setPackage(packageName)
        intent.putExtra("device_token", token.toByteArray())

        XLog.d(TAG, "mockReceive() called with: packageName = $packageName")

        context.sendBroadcast(intent)
    }
}