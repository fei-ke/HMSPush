package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import android.content.Context
import one.yufz.hmspush.common.HMSPUSH_PREF_NAME
import one.yufz.hmspush.common.PREF_KEY_DISABLE_SIGNATURE

object Prefs {
    private val pref = AndroidAppHelper.currentApplication().getSharedPreferences(HMSPUSH_PREF_NAME, Context.MODE_PRIVATE)

    internal fun setDisableSignature(disable: Boolean) {
        pref.edit().putBoolean(PREF_KEY_DISABLE_SIGNATURE, disable).apply()
    }

    fun isDisableSignature(): Boolean {
        return pref.getBoolean(PREF_KEY_DISABLE_SIGNATURE, false)
    }
}