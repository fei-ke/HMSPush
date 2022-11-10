package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.content.Context
import android.graphics.drawable.Icon
import one.yufz.hmspush.hook.hms.Prefs
import one.yufz.hmspush.hook.hms.icon.IconManager
import one.yufz.hmspush.hook.hms.nm.INotificationManager
import one.yufz.hmspush.hook.util.newBuilder

class IconHandler : NotificationHandler {
    override fun careAbout(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification): Boolean {
        return Prefs.prefModel.useCustomIcon
    }

    override fun handle(chain: NotificationHandler.Chain, manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        var newNotification = notification
        val iconData = IconManager.getNotificationIconData(context, packageName)
        if (iconData != null) {
            val builder = notification.newBuilder(context)
                .setSmallIcon(Icon.createWithBitmap(iconData.iconBitmap))

            if (Prefs.prefModel.tintIconColor) {
                iconData.iconColor?.let {
                    builder.setColor(it)
                }
            }

            newNotification = builder.build()
        }
        super.handle(chain, manager, context, packageName, id, newNotification)
    }
}