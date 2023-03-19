package one.yufz.hmspush.hook.hms

import android.content.Context
import one.yufz.xposed.AndroidAppHelper

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