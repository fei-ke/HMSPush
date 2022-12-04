package one.yufz.hmspush.app.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

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
            data = Uri.parse("package:${packageName}")
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            //ignore
        }
    }
}