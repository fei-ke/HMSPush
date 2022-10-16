package one.yufz.hmspush.common.model

import one.yufz.hmspush.common.content.ContentModel
import one.yufz.hmspush.common.content.ContentProperties

class ModuleVersionModel(var versionName: String, var versionCode: Int = -1) : ContentModel {
    constructor() : this(EMPTY_VERSION_NAME, EMPTY_VERSION_CODE)

    companion object {
        const val EMPTY_VERSION_NAME = ""
        const val EMPTY_VERSION_CODE = -1

        @JvmField
        val PROPERTIES = ContentProperties.Builder<ModuleVersionModel>()
            .property("versionName", ModuleVersionModel::versionName)
            .property("versionCode", ModuleVersionModel::versionCode)
            .build()
    }
}