package one.yufz.hmspush.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import org.json.JSONObject
import java.io.ByteArrayOutputStream

data class IconData(
    val appName: String,
    val packageName: String,
    val iconBitmap: Bitmap,
    val iconColor: Int?,
    val contributorName: String?
) {
    companion object {
        fun fromJson(json: String): IconData {
            return fromJson(JSONObject(json))
        }

        fun fromJson(obj: JSONObject): IconData {
            return IconData(
                appName = obj.getString("appName"),
                packageName = obj.getString("packageName"),
                iconBitmap = parseBitmap(obj.getString("iconBitmap")),
                iconColor = parseColor(obj.optString("iconColor")),
                contributorName = obj.optString("contributorName")
            )
        }

        private fun parseColor(colorString: String?): Int? {
            if (colorString == null || colorString.isEmpty()) {
                return null
            }

            return try {
                Color.parseColor(colorString)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                return null
            }
        }

        private fun parseBitmap(content: String): Bitmap {
            val bytes = Base64.decode(content, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        private fun Bitmap.toBase64(): String {
            val outputStream = ByteArrayOutputStream()
            compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        }

        private fun Int.toHexColorString(): String = String.format(
            "#%02X%02X%02X%02X",
            Color.alpha(this),
            Color.red(this),
            Color.green(this),
            Color.blue(this)
        )
    }

    fun toJson(): String {
        val obj = JSONObject().apply {
            put("appName", appName)
            put("packageName", packageName)
            put("iconBitmap", iconBitmap.toBase64())
            put("iconColor", iconColor?.toHexColorString())
            put("contributorName", contributorName)
        }
        return obj.toString()
    }
}

