package one.yufz.hmspush.hook.bridge

import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import one.yufz.hmspush.BuildConfig
import one.yufz.hmspush.common.AUTHORITIES
import one.yufz.hmspush.common.BridgeUri
import one.yufz.hmspush.common.PrefsModel
import one.yufz.hmspush.common.content.toContent
import one.yufz.hmspush.common.content.toCursor
import one.yufz.hmspush.hook.XLog
import one.yufz.hmspush.hook.hms.Prefs
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

    fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        XLog.d(TAG, "query() called with: uri = $uri, projection = $projection, selection = $selection, selectionArgs = $selectionArgs, sortOrder = $sortOrder")

        val code = uriMatcher.match(uri)
        return if (code != -1) {
            when (BridgeUri.values()[code]) {
                BridgeUri.PUSH_REGISTERED -> queryRegistered()
                BridgeUri.PUSH_HISTORY -> queryPushHistory()
                BridgeUri.MODULE_VERSION -> queryModuleVersion()
                BridgeUri.DISABLE_SIGNATURE -> queryIsDisableSignature()
                BridgeUri.PREFERENCES -> queryPreferences()
            }
        } else {
            null
        }
    }

    private fun queryPreferences(): Cursor {
        return Prefs.prefModel.toCursor()
    }

    fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        XLog.d(TAG, "update() called with: uri = $uri, values = $values, selection = $selection, selectionArgs = $selectionArgs")

        val code = uriMatcher.match(uri)
        if (code != -1) {
            return when (BridgeUri.values()[code]) {
                BridgeUri.PREFERENCES -> updatePreference(values)
                else -> {
                    0
                }
            }
        } else {
            return 0
        }
    }

    fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        XLog.d(TAG, "delete() called with: uri = $uri, selection = $selection, selectionArgs = $selectionArgs")

        val code = uriMatcher.match(uri)
        if (code != -1) {
            return when (BridgeUri.values()[code]) {
                BridgeUri.PUSH_REGISTERED -> unregister(selectionArgs)
                else -> {
                    0
                }
            }
        } else {
            return 0
        }

    }

    private fun queryIsDisableSignature(): Cursor? {
        return MatrixCursor(arrayOf("disableSignature")).apply {
            addRow(arrayOf(if (Prefs.isDisableSignature()) 1 else 0))
        }
    }

    private fun updatePreference(values: ContentValues?): Int {
        values ?: return 0

        values.toContent<PrefsModel>().let {
            Prefs.updatePreference(it)
        }

        return 1
    }


    private fun queryModuleVersion(): Cursor? {
        return MatrixCursor(arrayOf("versionName", "versionCode")).apply {
            addRow(arrayOf(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
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

    private fun unregister(args: Array<String>?): Int {
        args?.forEach {
            PushSignWatcher.unregisterSign(it)
            PushHistory.remove(it)
        }
        return 1
    }
}