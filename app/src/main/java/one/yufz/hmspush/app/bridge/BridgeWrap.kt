package one.yufz.hmspush.app.bridge

import android.content.Context
import android.database.Cursor
import android.net.Uri
import one.yufz.hmspush.common.BridgeUri

object BridgeWrap {
    private fun query(context: Context, uri: Uri): Cursor? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
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

}