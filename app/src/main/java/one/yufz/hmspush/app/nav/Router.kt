package one.yufz.hmspush.app.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Router : NavKey {

    @Serializable
    data object Home : Router

    @Serializable
    data object Settings : Router

    @Serializable
    data object Icon : Router

    @Serializable
    data object FakeDevice : Router

    /** packageName 非空时只显示该应用的推送记录 */
    @Serializable
    data class PushHistory(val packageName: String? = null) : Router
}