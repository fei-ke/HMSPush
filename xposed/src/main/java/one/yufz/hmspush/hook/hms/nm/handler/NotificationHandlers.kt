package one.yufz.hmspush.hook.hms.nm.handler

import android.app.Notification
import android.content.Context
import one.yufz.hmspush.hook.hms.nm.INotificationManager
import java.util.*

object NotificationHandlers {
    private val handlers = listOf(
        GroupNotificationHandler(),
        GroupByIdHandler(),
        FinalHandler()
    )

    fun handle(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
        HandlerChain(LinkedList(handlers))
            .proceed(manager, context, packageName, id, notification)
    }

    class HandlerChain(private val linkList: LinkedList<NotificationHandler>) : NotificationHandler.Chain {
        override fun proceed(manager: INotificationManager, context: Context, packageName: String, id: Int, notification: Notification) {
            val head = linkList.poll()

            if (head != null) {
                if (head.careAbout(manager, context, packageName, id, notification)) {
                    head.handle(this, manager, context, packageName, id, notification)
                } else {
                    this.proceed(manager, context, packageName, id, notification)
                }
            }
        }
    }
}