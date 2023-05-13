package one.yufz.hmspush.app

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import one.yufz.hmspush.app.fake.FakeDeviceScreen
import one.yufz.hmspush.app.home.HomeScreen
import one.yufz.hmspush.app.icon.IconScreen
import one.yufz.hmspush.app.settings.SettingsScreen

val LocalNavHostController = staticCompositionLocalOf<NavHostController> { error("shouldn't happen") }

object Routers {
    const val FAKE_DEVICE = "fake_device"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "home"
) {
    CompositionLocalProvider(LocalNavHostController provides navController) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween()) },
            exitTransition = { fadeOut(animationSpec = tween()) },
        ) {
            composable("home") { HomeScreen() }
            composable("settings") { SettingsScreen() }
            composable("icon") { IconScreen() }
            composable(Routers.FAKE_DEVICE) { FakeDeviceScreen() }
        }
    }
}
