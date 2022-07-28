package one.yufz.hmspush

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent().apply {
            setClassName(HMS_PACKAGE_NAME, HMS_CORE_DUMMY_SETTINGS_ACTIVITY)
            putExtra(FLAG_HMS_PUSH_SETTINGS, true)
        }
        startActivity(intent)
        finish()
    }
}