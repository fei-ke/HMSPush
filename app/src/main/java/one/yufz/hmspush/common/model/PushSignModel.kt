package one.yufz.hmspush.common.model

import one.yufz.hmspush.common.content.ContentModel
import one.yufz.hmspush.common.content.ContentProperties

class PushSignModel(var packageName: String, var userId: Int) : ContentModel {
    constructor() : this("", 0)

    companion object {
        @JvmField
        val PROPERTIES = ContentProperties.Builder<PushSignModel>()
            .property("packageName", PushSignModel::packageName)
            .property("userId", PushSignModel::userId)
            .build()
    }
}