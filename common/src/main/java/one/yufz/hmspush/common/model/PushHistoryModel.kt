package one.yufz.hmspush.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class PushHistoryModel constructor(var packageName: String, var pushTime: Long) : Parcelable