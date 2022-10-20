package one.yufz.hmspush.common

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri

object BridgeWrap {
    fun registerObserve(context: Context, uri: Uri, observer: ContentObserver) {
        context.contentResolver.registerContentObserver(uri, false, observer)
    }

    fun unregisterObserve(context: Context, observer: ContentObserver) {
        context.contentResolver.unregisterContentObserver(observer)
    }

    fun query(context: Context, uri: Uri): Cursor? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }
    }

    fun isDisableSignature(context: Context): Boolean {
        query(context, BridgeUri.DISABLE_SIGNATURE.toUri())?.use {
            val indexDisableSignature = it.getColumnIndex("disableSignature")

            if (it.moveToNext()) {
                return it.getInt(indexDisableSignature) == 1
            }
        }
        return false
    }
}