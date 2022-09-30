package one.yufz.hmspush.hook.bridge

import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import one.yufz.hmspush.hook.hms.PushHistory
import one.yufz.hmspush.hook.hms.PushSignWatcher

class BridgeContentProvider {
    companion object {
        private const val TAG = "BridgeContentProvider"

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            BridgeUri.values().forEach {
                uriMatcher.addURI(AUTHORITIES, it.path, it.ordinal)
            }
        }
    }

    fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        return null
    }

    fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val code = uriMatcher.match(uri)
        return if (code != -1) {
            when (BridgeUri.values()[code]) {
                BridgeUri.PUSH_REGISTERED -> queryRegistered()
                BridgeUri.PUSH_HISTORY -> queryPushHistory()
                BridgeUri.PUSH_UNREGISTER -> unregister(uri)
            }
        } else {
            null
        }
    }


    private fun queryRegistered(): Cursor? {
        val cursor = MatrixCursor(arrayOf("packageName"))
        PushSignWatcher.lastRegistered.forEach {
            val split = it.split("/")
            cursor.addRow(arrayOf(split[0]))
        }
        return cursor
    }

    private fun queryPushHistory(): Cursor? {
        val cursor = MatrixCursor(arrayOf("packageName", "time"))
        PushHistory.getAll().forEach {
            cursor.addRow(arrayOf(it.key, it.value))
        }
        return cursor
    }

    private fun unregister(uri: Uri): Cursor? {
        val packageName = uri.getQueryParameter("packageName")
        if (packageName != null) {
            PushSignWatcher.unregisterSign(packageName)
            return MatrixCursor(emptyArray())
        }
        return null
    }
}