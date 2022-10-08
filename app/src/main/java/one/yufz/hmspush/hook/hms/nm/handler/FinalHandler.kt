package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.content.Context
import one.yufz.hmspush.hook.XLog
import one.yufz.hmspush.hook.hms.nm.INotificationManager

class FinalHandler : NotificationHandler {
    companion object {
        private const val TAG = "FinalHandler"
    }

    override fun careAbout(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification): Boolean {
        return true
    }

    override fun handle(chain: NotificationHandler.Chain, manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        XLog.d(TAG, "handle() called with: packageName = $packageName, id = $id, notification = $notification")
        manager.notify(context, packageName, id, notification)
    }
}