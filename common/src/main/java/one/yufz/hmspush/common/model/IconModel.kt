package one.yufz.hmspush.common.model

import android.os.ParcelFileDescriptor
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import one.yufz.hmspush.common.IconData
import java.io.FileReader

@Parcelize
class IconModel(val packageName: String, val iconData: String? = null, val dataFD: ParcelFileDescriptor? = null) : Parcelable {

    fun toIconData(): IconData? {
        if (iconData != null) {
            return try {
                IconData.fromJson(iconData)
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        }

        if (dataFD != null) {
            try {
                FileReader(dataFD.fileDescriptor).use {
                    return IconData.fromJson(it.readText())
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                return null
            }
        }
        throw IllegalStateException("iconData and dataFD all be null")
    }
}