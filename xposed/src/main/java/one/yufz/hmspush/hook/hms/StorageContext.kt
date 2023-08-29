package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import android.content.Context

object StorageContext {

    fun get(): Context {
        return AndroidAppHelper.currentApplication().createDeviceProtectedStorageContext()
    }
}