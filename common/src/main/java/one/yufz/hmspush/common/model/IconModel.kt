package one.yufz.hmspush.common.model

import android.os.ParcelFileDescriptor
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import one.yufz.hmspush.common.IconData
import java.io.FileReader

@Parcelize
class IconModel(val packageName: String, val iconData: String? = null, val dataFD: ParcelFileDescriptor? = null) : Parcelable {

    fun toIconData(): IconData {
        if (iconData != null) {
            return IconData.fromJson(iconData)
        }
        if (dataFD != null) {
            FileReader(dataFD.fileDescriptor).use {
                return IconData.fromJson(it.readText())
            }
        }
        throw IllegalStateException("iconData and dataFD all be null")
    }
}