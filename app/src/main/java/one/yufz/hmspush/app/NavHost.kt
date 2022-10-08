package one.yufz.hmspush.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import one.yufz.hmspush.app.home.HomeScreen
import one.yufz.hmspush.app.settings.SettingsScreen

val LocalNavHostController = staticCompositionLocalOf<NavHostController> { error("shouldn't happen") }

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
            startDestination = startDestination
        ) {
            composable("home") { HomeScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
