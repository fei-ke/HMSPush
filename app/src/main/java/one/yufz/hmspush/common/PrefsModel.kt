package one.yufz.hmspush.common

import one.yufz.hmspush.common.content.ContentModel
import one.yufz.hmspush.common.content.ContentProperties

data class PrefsModel @JvmOverloads constructor(
    var disableSignature: Boolean = false,
    var enhanceQQMessage: Boolean = true
) : ContentModel {

    companion object {
        @JvmField
        val PROPERTIES = ContentProperties.Builder<PrefsModel>()
            .property("disableSignature", PrefsModel::disableSignature)
            .property("enhanceQQMessage", PrefsModel::enhanceQQMessage)
            .build()
    }
}
