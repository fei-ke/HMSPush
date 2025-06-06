package one.yufz.hmspush.hook.bridge

import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import one.yufz.hmspush.common.AUTHORITIES
import one.yufz.hmspush.common.BinderCursor
import one.yufz.hmspush.common.BridgeUri
import one.yufz.hmspush.hook.XLog
import one.yufz.hmspush.hook.hms.HmsPushService
import one.yufz.hmspush.hook.hms.Prefs

class BridgeContentProvider {
    companion object {
        private const val TAG = "BridgeContentProvider"

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            BridgeUri.entries.forEach {
                uriMatcher.addURI(AUTHORITIES, it.path, it.ordinal)
            }
        }
    }

    fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        XLog.d(TAG, "query() called with: uri = $uri, projection = $projection, selection = $selection, selectionArgs = $selectionArgs, sortOrder = $sortOrder")

        val code = uriMatcher.match(uri)
        return if (code != -1) {
            when (BridgeUri.entries[code]) {
                BridgeUri.DISABLE_SIGNATURE -> queryIsDisableSignature()
                BridgeUri.HMS_PUSH_SERVICE -> BinderCursor(HmsPushService)
                else -> throw IllegalStateException("Unsupported")
            }
        } else {
            null
        }
    }

    private fun queryIsDisableSignature(): Cursor? {
        return MatrixCursor(arrayOf("disableSignature")).apply {
            addRow(arrayOf(if (Prefs.isDisableSignature()) 1 else 0))
        }
    }
}
