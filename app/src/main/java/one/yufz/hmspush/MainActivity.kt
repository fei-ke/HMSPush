package one.yufz.hmspush

import android.app.Activity
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import one.yufz.hmspush.app.bridge.BridgeWrap
import one.yufz.hmspush.app.settings.AppListFragment
import one.yufz.hmspush.common.HMS_PACKAGE_NAME

class MainActivity : Activity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        makeActivityFullScreen()
    }

    override fun onResume() {
        super.onResume()
        if (checkHmsCore()) {
            addAppListFragment()
        } else {
            removeAppListFragment()
        }
    }

    private fun removeAppListFragment() {
        val fragment = fragmentManager.findFragmentByTag("app_list")
        if (fragment != null) {
            fragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }
    }

    private fun addAppListFragment() {
        if (fragmentManager.findFragmentByTag("app_list") != null) return

        Log.d(TAG, "addAppListFragment")

        fragmentManager.beginTransaction()
            .replace(Window.ID_ANDROID_CONTENT, AppListFragment(), "app_list")
            .commit()
    }

    private fun checkHmsCore(): Boolean {
        try {
            packageManager.getApplicationInfo(HMS_PACKAGE_NAME, 0)
        } catch (e: NameNotFoundException) {
            inflateTips(R.string.hms_core_not_found)
            return false
        }

        val moduleVersion = BridgeWrap.getModuleVersion(this)
        if (moduleVersion == null || moduleVersion.first != BuildConfig.VERSION_NAME) {
            inflateTips(R.string.module_not_activated)
            return false
        }
        return true
    }

    private fun inflateTips(tipRes: Int) {
        setContentView(R.layout.tips)
        findViewById<TextView>(R.id.tips).setText(tipRes)
    }

    private fun makeActivityFullScreen() {
        window.statusBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}