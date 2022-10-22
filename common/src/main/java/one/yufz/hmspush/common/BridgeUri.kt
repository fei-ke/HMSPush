package one.yufz.hmspush.common

import android.content.Context
import android.net.Uri

const val AUTHORITIES = "com.huawei.hms"

enum class BridgeUri(val path: String) {
    PUSH_SIGN("hmspush/sign"),
    PUSH_HISTORY("hmspush/history"),
    DISABLE_SIGNATURE("hmspush/disableSignature"),
    HMS_PUSH_SERVICE("hmspush/service");

    override fun toString(): String {
        return "content://$AUTHORITIES/$path"
    }

    fun notifyContentChanged(context: Context) {
        context.contentResolver.notifyChange(toUri(), null, false)
    }

    fun toUri(): Uri = Uri.parse(toString())
}