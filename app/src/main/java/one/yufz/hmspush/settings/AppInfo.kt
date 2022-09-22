package one.yufz.hmspush.settings

class AppInfo(
    val packageName: String,
    val name: String,
    var registered: Boolean = false,
    val lastPushTime: Long? = null
)