package one.yufz.hmspush.common

import android.net.Uri

const val AUTHORITIES = "com.huawei.hms"

enum class BridgeUri(val path: String) {
    PUSH_REGISTERED("hmspush/registered"),
    PUSH_HISTORY("hmspush/history"),
    PUSH_UNREGISTER("hmspush/unregister"),
    MODULE_VERSION("hmspush/moduleVersion");

    override fun toString(): String {
        return "content://$AUTHORITIES/$path"
    }

    fun toUri(): Uri = Uri.parse(toString())
}