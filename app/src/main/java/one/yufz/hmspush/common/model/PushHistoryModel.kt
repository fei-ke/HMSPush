package one.yufz.hmspush.common.model

import one.yufz.hmspush.common.content.ContentModel
import one.yufz.hmspush.common.content.ContentProperties

class PushHistoryModel constructor(var packageName: String, var pushTime: Long) : ContentModel {
    constructor() : this("", 0)

    companion object {
        @JvmField
        val PROPERTIES = ContentProperties.Builder<PushHistoryModel>()
            .property("packageName", PushHistoryModel::packageName)
            .property("pushTime", PushHistoryModel::pushTime)
            .build()
    }
}