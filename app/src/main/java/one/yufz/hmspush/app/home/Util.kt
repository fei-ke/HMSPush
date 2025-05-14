package one.yufz.hmspush.app.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

object Util {
    fun launchApp(context: Context, packageName: String) {
        val launchIntentForPackage = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntentForPackage != null) {
            context.startActivity(launchIntentForPackage)
        }
    }

    fun launchAppInfo(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = "package:${packageName}".toUri()
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            //ignore
        }
    }
}
