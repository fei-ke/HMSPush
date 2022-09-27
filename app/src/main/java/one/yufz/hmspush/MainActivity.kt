package one.yufz.hmspush

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toolbar
import one.yufz.hmspush.app.bridge.BridgeWrap
import one.yufz.hmspush.app.settings.AppListFragment
import one.yufz.hmspush.common.HMS_PACKAGE_NAME

class MainActivity : Activity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        makeActivityFullScreen()

        val toolbar = findViewById<Toolbar>(R.id.toolBar)

        setActionBar(toolbar)

        findViewById<View>(R.id.root).setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(0, insets.systemWindowInsetTop, 0, 0)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkHmsCoreAndShowTips()) {
            addAppListFragment()
        } else {
            removeAllFragment()
        }
    }

    private fun addAppListFragment() {
        if (fragmentManager.findFragmentByTag("app_list") != null) return

        Log.d(TAG, "addAppListFragment")

        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AppListFragment(), "app_list")
            .commit()
    }

    private fun removeAllFragment() {
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun checkHmsCoreAndShowTips(): Boolean {
        try {
            packageManager.getApplicationInfo(HMS_PACKAGE_NAME, 0)
        } catch (e: NameNotFoundException) {
            showTips(R.string.hms_core_not_found)
            return false
        }

        val moduleVersion = BridgeWrap.getModuleVersion(this)
        if (moduleVersion == null || moduleVersion.first != BuildConfig.VERSION_NAME) {
            showTips(R.string.hms_not_activated)
            return false
        }
        hideTips()
        return true
    }

    private fun showTips(tipRes: Int) {
        val tipsView = findViewById<TextView>(R.id.tips)
        tipsView.visibility = View.VISIBLE
        tipsView.setText(tipRes)
    }

    private fun hideTips() {
        val tipsView = findViewById<TextView>(R.id.tips)
        tipsView.visibility = View.GONE
        tipsView.text = ""
    }

    private fun makeActivityFullScreen() {
        window.statusBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    fun pushFragment(fragment: Fragment, tag: String) {
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.animator.fragment_fade_enter, R.animator.fragment_fade_exit, R.animator.fragment_fade_enter, R.animator.fragment_fade_exit)
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    fun popupFragment() {
        fragmentManager.popBackStack()
    }
}