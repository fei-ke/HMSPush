package one.yufz.hmspush.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

object HmsCoreUtil {
    fun startHmsCoreService(context: Context, foreground: Boolean) {
        val intent = createHmsCoreServiceIntent().apply {
            putExtra(KEY_HMS_CORE_EXPLICIT_FOREGROUND, foreground)
        }
        if (foreground) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun createHmsCoreServiceIntent(): Intent {
        return Intent(HMS_CORE_SERVICE_ACTION).apply {
            setClassName(HMS_PACKAGE_NAME, HMS_CORE_SERVICE)
        }
    }

    fun createHmsCoreDummyActivityIntent(): Intent {
        return Intent().apply {
            setClassName(HMS_PACKAGE_NAME, HMS_CORE_DUMMY_ACTIVITY)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(FLAG_HMS_DUMMY_HOOKED, true)
        }
    }

    fun startHmsCoreDummyActivity(context: Context) {
        try {
            context.startActivity(createHmsCoreDummyActivityIntent())
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}