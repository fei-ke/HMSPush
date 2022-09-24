package one.yufz.hmspush.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.provider.Settings
import one.yufz.hmspush.BuildConfig
import one.yufz.hmspush.PushHistory
import one.yufz.hmspush.PushSignWatcher

fun Context.dp2px(dp: Number): Int = (dp.toFloat() * resources.displayMetrics.density + 0.5f).toInt()

@Throws(NameNotFoundException::class, SecurityException::class)
fun Context.createModuleContext() = createPackageContext(BuildConfig.APPLICATION_ID, 0)

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
        context.startActivity(intent)
    }

    fun unregisterPush(packageName: String) {
        PushSignWatcher.unregisterSign(packageName)
        PushHistory.remove(packageName)
    }
}