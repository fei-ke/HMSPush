package one.yufz.hmspush

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method

object XLog {
    fun d(tag: String, message: String?) {
        XposedBridge.log("[HMSPush]  $tag  $message")
    }

    fun i(tag: String, message: String?) {
        XposedBridge.log("[HMSPush]  $tag  $message")
    }

    fun e(tag: String, message: String?, throwable: Throwable) {
        i(tag, message)
        XposedBridge.log(throwable)
    }

    fun XC_MethodHook.MethodHookParam.logMethod(tag: String) {
        d(tag, "╔═══════════════════════════════════════════════════════")
        d(tag, method.toString())
        d(tag, "${method.name} called with ${args.contentToString()}")

        if (hasThrowable()) {
            e(tag, "${method.name} thrown", throwable)
        } else if (method is Method && (method as Method).returnType != Void.TYPE) {
            d(tag, "${method.name} return $result")
        }
        d(tag, "╚═══════════════════════════════════════════════════════")
    }
}