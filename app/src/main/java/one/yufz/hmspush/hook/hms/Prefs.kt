package one.yufz.hmspush.hook.hms

import android.app.AndroidAppHelper
import android.content.Context
import one.yufz.hmspush.common.HMSPUSH_PREF_NAME
import one.yufz.hmspush.common.PREF_KEY_DISABLE_SIGNATURE
import one.yufz.hmspush.common.PrefsModel
import one.yufz.hmspush.common.content.storeToSharedPreference
import one.yufz.hmspush.common.content.toContent

object Prefs {
    private val pref = AndroidAppHelper.currentApplication().getSharedPreferences(HMSPUSH_PREF_NAME, Context.MODE_PRIVATE)

    var prefModel: PrefsModel
        private set

    init {
        prefModel = pref.toContent()
        migrateLegacyPreference()
    }

    private fun migrateLegacyPreference() {
        if (pref.contains(PREF_KEY_DISABLE_SIGNATURE)) {
            updatePreference(
                prefModel.copy(disableSignature = pref.getBoolean(PREF_KEY_DISABLE_SIGNATURE, false))
            )
        }
    }

    fun updatePreference(prefsModel: PrefsModel) {
        this.prefModel = prefsModel.also { model ->
            pref.edit()
                .also { model.storeToSharedPreference(it) }
                .apply()
        }

    }

    fun isDisableSignature(): Boolean {
        return pref.getBoolean(PREF_KEY_DISABLE_SIGNATURE, false)
    }

}