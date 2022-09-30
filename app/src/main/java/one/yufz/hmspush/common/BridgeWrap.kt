package one.yufz.hmspush.common

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

object BridgeWrap {
    private fun query(context: Context, uri: Uri): Cursor? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }
    }

    private fun update(context: Context, uri: Uri, values: ContentValues): Int {
        try {
            return context.contentResolver.update(uri, values, null, null)
        } catch (e: SecurityException) {
            e.printStackTrace()
            return 0
        }
    }

    private fun delete(context: Context, uri: Uri, args: Array<String>?): Int {
        try {
            return context.contentResolver.delete(uri, null, args)
        } catch (e: SecurityException) {
            e.printStackTrace()
            return 0
        }
    }

    fun getRegistered(context: Context): Set<String> {
        query(context, BridgeUri.PUSH_REGISTERED.toUri())?.use {
            val set = HashSet<String>()

            val indexPackageName = it.getColumnIndex("packageName")

            while (it.moveToNext()) {
                val packageName = it.getString(indexPackageName)
                set.add(packageName)
            }
            return set
        }

        return emptySet()
    }

    fun getPushHistory(context: Context): Map<String, Long> {
        query(context, BridgeUri.PUSH_HISTORY.toUri())?.use {
            val map = HashMap<String, Long>()

            val indexPackageName = it.getColumnIndex("packageName")
            val indexTime = it.getColumnIndex("time")

            while (it.moveToNext()) {
                val packageName = it.getString(indexPackageName)
                val time = it.getLong(indexTime)
                map[packageName] = time
            }
            return map
        }

        return emptyMap()
    }

    fun getModuleVersion(context: Context): Pair<String, Int>? {
        query(context, BridgeUri.MODULE_VERSION.toUri())?.use {
            val indexVersionName = it.getColumnIndex("versionName")
            val indexVersionCode = it.getColumnIndex("versionCode")

            if (it.moveToNext()) {
                return it.getString(indexVersionName) to it.getInt(indexVersionCode)
            }
        }

        return null
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

    fun setDisableSignature(context: Context, disableSignature: Boolean): Boolean {
        val values = ContentValues().apply {
            put("disableSignature", disableSignature)
        }

        return update(context, BridgeUri.DISABLE_SIGNATURE.toUri(), values) > 0
    }

    fun unregisterPush(context: Context, packageName: String) {
        delete(context, BridgeUri.PUSH_REGISTERED.toUri(), arrayOf(packageName))
    }
}