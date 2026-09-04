package one.yufz.hmspush.app.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import one.yufz.hmspush.app.fake.FakeDeviceScreen
import one.yufz.hmspush.app.history.PushHistoryScreen
import one.yufz.hmspush.app.home.HomeScreen
import one.yufz.hmspush.app.icon.IconScreen
import one.yufz.hmspush.app.settings.SettingsScreen

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No LocalNavigator provided") }

val appEntryProvider = entryProvider<NavKey> {
    entry<Router.Home> { HomeScreen() }
    entry<Router.Settings> { SettingsScreen() }
    entry<Router.Icon> { IconScreen() }
    entry<Router.FakeDevice> { FakeDeviceScreen() }
    entry<Router.PushHistory> { PushHistoryScreen(packageName = it.packageName) }
}

@Composable
fun AppNavDisplay(
    modifier: Modifier = Modifier,
    startDestination: NavKey = Router.Home
) {
    val navigationState = rememberNavigationState(
        startRoute = startDestination,
        topLevelRoutes = setOf(Router.Home)
    )
    val navigator = remember { Navigator(navigationState) }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            modifier = modifier,
            entries = navigationState.toEntries(appEntryProvider),
            onBack = { navigator.goBack() },
        )
    }
}