package one.yufz.xposed

import android.app.Application

object AndroidAppHelper {
    fun currentApplication(): Application {
        return xposed.currentApplication()
    }
}