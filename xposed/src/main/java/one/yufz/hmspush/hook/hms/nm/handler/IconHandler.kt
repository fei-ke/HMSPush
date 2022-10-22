package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.content.Context
import android.graphics.drawable.Icon
import one.yufz.hmspush.hook.hms.Prefs
import one.yufz.hmspush.hook.hms.icon.IconManager
import one.yufz.hmspush.hook.hms.nm.INotificationManager
import one.yufz.xposed.set

class IconHandler : NotificationHandler {
    override fun careAbout(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification): Boolean {
        return Prefs.prefModel.useCustomIcon
    }

    override fun handle(chain: NotificationHandler.Chain, manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        val iconData = IconManager.getIconData(packageName)
        if (iconData != null) {
            notification["mSmallIcon"] = Icon.createWithBitmap(iconData.iconBitmap)
            iconData.iconColor?.let {
                notification["color"] = it
            }
        }
        super.handle(chain, manager, context, packageName, id, notification)
    }
}