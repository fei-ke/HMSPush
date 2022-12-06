package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import android.content.Context

object StorageContext {
    var useDeviceProtectedStorageContext = false

    fun get(): Context {
        return if (useDeviceProtectedStorageContext) {
            AndroidAppHelper.currentApplication().createDeviceProtectedStorageContext()
        } else {
            AndroidAppHelper.currentApplication()
        }
    }
}