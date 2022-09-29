package one.yufz.hmspush.hook.hms.settings

class AppInfo(
    val packageName: String,
    val name: String,
    var registered: Boolean = false,
    val lastPushTime: Long? = null
)