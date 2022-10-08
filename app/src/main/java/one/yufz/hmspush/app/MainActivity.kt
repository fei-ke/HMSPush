package one.yufz.hmspush.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import one.yufz.hmspush.app.AppNavHost
import one.yufz.hmspush.app.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeActivityFullScreen()
        setContent {
            AppTheme {
                AppNavHost()
            }
        }
    }


    private fun makeActivityFullScreen() {
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}