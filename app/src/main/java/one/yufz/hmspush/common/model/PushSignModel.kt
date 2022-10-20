package one.yufz.hmspush.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import one.yufz.hmspush.common.content.ContentModel
import one.yufz.hmspush.common.content.ContentProperties

@Parcelize
class PushSignModel(var packageName: String, var userId: Int) : ContentModel, Parcelable {
    constructor() : this("", 0)

    companion object {
        @JvmField
        val PROPERTIES = ContentProperties.Builder<PushSignModel>()
            .property("packageName", PushSignModel::packageName)
            .property("userId", PushSignModel::userId)
            .build()
    }
}