package one.yufz.hmspush.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class PushSignModel(var packageName: String, var userId: Int) : Parcelable