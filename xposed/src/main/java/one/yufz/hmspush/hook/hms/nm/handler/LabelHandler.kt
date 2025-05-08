package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.content.Context
import one.yufz.hmspush.hook.XLog
import one.yufz.hmspush.hook.hms.nm.INotificationManager
import one.yufz.hmspush.hook.hms.nm.SelfNotificationManager
import one.yufz.hmspush.hook.util.newBuilder

class LabelHandler : NotificationHandler {
    companion object {
        private const val TAG = "LabelHandler"
    }

    override fun careAbout(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification): Boolean {
        return manager is SelfNotificationManager
    }

    override fun handle(chain: NotificationHandler.Chain, manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        val applicationName = getApplicationName(context, packageName)
        val n = if (applicationName.isNullOrEmpty()) {
            notification
        } else {
            notification.newBuilder(context)
                .setSubText(applicationName)
                .build()
        }
        super.handle(chain, manager, context, packageName, id, n)
    }

    private fun getApplicationName(context: Context, packageName: String): CharSequence? {
        try {
            val pm = context.packageManager
            return pm.getApplicationInfo(packageName, 0).loadLabel(pm)
        } catch (e: Throwable) {
            XLog.e(TAG, "getApplicationName: error", e)
            return null
        }
    }
}
