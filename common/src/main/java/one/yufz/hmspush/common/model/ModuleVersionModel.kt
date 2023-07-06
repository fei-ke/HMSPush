package one.yufz.hmspush.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ModuleVersionModel(
    var versionName: String,
    var versionCode: Int = -1,
    var apiVersion: Int = 0
) : Parcelable