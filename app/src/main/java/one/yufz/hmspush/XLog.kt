package one.yufz.hmspush

import de.robv.android.xposed.XposedBridge

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
}