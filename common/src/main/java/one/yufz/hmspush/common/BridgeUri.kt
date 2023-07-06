package one.yufz.hmspush.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri

const val AUTHORITIES = "com.huawei.hms"

//android.content.ContentResolver#NOTIFY_NO_DELAY
private const val NOTIFY_NO_DELAY = 1 shl 15

enum class BridgeUri(val path: String) {
    PUSH_SIGN("hmspush/sign"),
    PUSH_HISTORY("hmspush/history"),
    DISABLE_SIGNATURE("hmspush/disableSignature"),
    HMS_PUSH_SERVICE("hmspush/service");

    override fun toString(): String {
        return "content://$AUTHORITIES/$path"
    }

    @SuppressLint("WrongConstant")
    fun notifyContentChanged(context: Context) {
        context.contentResolver.notifyChange(toUri(), null, NOTIFY_NO_DELAY)
    }

    fun toUri(): Uri = Uri.parse(toString())
}