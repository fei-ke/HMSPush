package one.yufz.hmspush

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import one.yufz.hmspush.common.FLAG_HMS_PUSH_SETTINGS
import one.yufz.hmspush.common.HMS_CORE_DUMMY_SETTINGS_ACTIVITY
import one.yufz.hmspush.common.HMS_PACKAGE_NAME

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent().apply {
            setClassName(HMS_PACKAGE_NAME, HMS_CORE_DUMMY_SETTINGS_ACTIVITY)
            putExtra(FLAG_HMS_PUSH_SETTINGS, true)
        }
        try {
            startActivity(intent)
            finish()
        } catch (e: ActivityNotFoundException) {
            inflateTips()
        }
    }

    private fun inflateTips() {
        window.statusBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContentView(R.layout.tips)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        finishAfterTransition()
    }
}