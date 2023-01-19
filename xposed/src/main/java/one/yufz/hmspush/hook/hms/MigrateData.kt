package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import android.content.Context
import one.yufz.hmspush.common.HMSPUSH_PREF_NAME
import one.yufz.hmspush.common.content.putValue
import one.yufz.hmspush.hook.XLog
import java.io.File

private const val TAG = "MigrateData"

private val context: Context
    get() = AndroidAppHelper.currentApplication()

private val storageContext: Context
    get() = context.createDeviceProtectedStorageContext()

@Deprecated("Remove this after 2023-03-01")
fun migrateDataToDeviceProtectedStorage(): Boolean {
    var result = migratePreferences(HMSPUSH_PREF_NAME)
    result = result && migratePreferences("push_history")
    result = result && migrateFilesDir("hms_push/icons")

    XLog.d(TAG, "migrateDataToDeviceProtectedStorage() returned: $result")

    return result
}

private fun migratePreferences(name: String): Boolean {
    XLog.d(TAG, "migratePreferences() called with: name = $name")

    try {
        val newPrefs = storageContext.getSharedPreferences(name, Context.MODE_PRIVATE)
        if (newPrefs.all.isNotEmpty()) return true

        val oldPrefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        val oldMap = oldPrefs.all
        if (oldMap.isEmpty()) return true

        oldMap.forEach { (t, v) ->
            if (v != null) {
                newPrefs.edit().putValue(t, v::class, v).apply()
            }
        }
        oldPrefs.edit().clear().apply()
        return true
    } catch (e: IllegalStateException) {
        XLog.e(TAG, "Failed to migrate preferences $name", e)
        return false
    }
}

private fun migrateFilesDir(filesDirPath: String): Boolean {
    XLog.d(TAG, "migrateFilesDir() called with: filesDirPath = $filesDirPath")

    try {
        val newDir = File(storageContext.filesDir, filesDirPath)

        if (newDir.exists()) return true

        val oldDir = File(context.filesDir, filesDirPath)
        if (!oldDir.exists()) return true

        if (oldDir.isDirectory && oldDir.listFiles().isNullOrEmpty()) return true

        oldDir.copyRecursively(newDir, true)

        oldDir.deleteRecursively()

        return true
    } catch (e: IllegalStateException) {
        XLog.e(TAG, "Failed to migrate files dir $filesDirPath", e)
        return false
    }

}