package one.yufz.hmspush.app.home

data class AppInfo(
    val packageName: String,
    val name: String,
    var registered: Boolean = false,
    val lastPushTime: Long? = null,
    val useZygiskFake: Boolean = false,
)