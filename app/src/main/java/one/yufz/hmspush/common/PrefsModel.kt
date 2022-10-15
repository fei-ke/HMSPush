package one.yufz.hmspush.common

import one.yufz.hmspush.common.content.ContentModel
import one.yufz.hmspush.common.content.ContentProperties

data class PrefsModel constructor(
    var disableSignature: Boolean,
    var groupMessageById: Boolean,
) : ContentModel {
    constructor() : this(false, true)

    companion object {
        @JvmField
        val PROPERTIES = ContentProperties.Builder<PrefsModel>()
            .property("disableSignature", PrefsModel::disableSignature)
            .property("groupMessageById", PrefsModel::groupMessageById)
            .build()
    }
}
