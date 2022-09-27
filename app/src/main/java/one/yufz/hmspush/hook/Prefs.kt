package one.yufz.hmspush.hook

import de.robv.android.xposed.XSharedPreferences
import one.yufz.hmspush.BuildConfig
import one.yufz.hmspush.common.HMSPUSH_PREF_NAME
import one.yufz.hmspush.common.PREF_KEY_DISABLE_SIGNATURE

object Prefs {
    private val pref = XSharedPreferences(BuildConfig.APPLICATION_ID, HMSPUSH_PREF_NAME)

    fun isDisableSignature(): Boolean {
        pref.reload()
        return pref.getBoolean(PREF_KEY_DISABLE_SIGNATURE, false)
    }
}