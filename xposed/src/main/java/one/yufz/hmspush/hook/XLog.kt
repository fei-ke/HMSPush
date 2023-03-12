package one.yufz.hmspush.hook

import android.util.Log
import one.yufz.xposed.MethodHookParam
import one.yufz.xposed.log
import java.lang.reflect.Method

object XLog {
    fun d(tag: String, message: String?) {
        log("[HMSPush]  $tag  $message")
    }

    fun i(tag: String, message: String?) {
        log("[HMSPush]  $tag  $message")
    }

    fun e(tag: String, message: String?, throwable: Throwable?) {
        i(tag, message)
        i(tag, Log.getStackTraceString(throwable))
    }

    fun MethodHookParam.logMethod(tag: String, stackTrace: Boolean = false) {
        d(tag, "╔═══════════════════════════════════════════════════════")
        d(tag, method.toString())
        d(tag, "${method.name} called with ${args.contentDeepToString()}")
        if (stackTrace) {
            d(tag, Log.getStackTraceString(Throwable()))
        }
        if (hasThrowable()) {
            e(tag, "${method.name} thrown", throwable)
        } else if (method is Method && (method as Method).returnType != Void.TYPE) {
            d(tag, "${method.name} return $result")
        }
        d(tag, "╚═══════════════════════════════════════════════════════")
    }
}