package one.yufz.hmspush.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import one.yufz.hmspush.common.content.ContentModel
import one.yufz.hmspush.common.content.ContentProperties

@Parcelize
class PushHistoryModel constructor(var packageName: String, var pushTime: Long) : ContentModel, Parcelable {
    constructor() : this("", 0)

    companion object {
        @JvmField
        val PROPERTIES = ContentProperties.Builder<PushHistoryModel>()
            .property("packageName", PushHistoryModel::packageName)
            .property("pushTime", PushHistoryModel::pushTime)
            .build()
    }
}