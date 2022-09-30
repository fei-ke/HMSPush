package one.yufz.hmspush.app.settings

class AppInfo(
    val packageName: String,
    val name: String,
    var registered: Boolean = false,
    val lastPushTime: Long? = null
)