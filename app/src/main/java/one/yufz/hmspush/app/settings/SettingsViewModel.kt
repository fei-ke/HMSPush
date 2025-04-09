package one.yufz.hmspush.app.settings

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import one.yufz.hmspush.app.HmsPushClient
import one.yufz.hmspush.app.mainActivityAlias
import one.yufz.hmspush.common.HmsCoreUtil
import one.yufz.hmspush.common.model.PrefsModel

class SettingsViewModel(val context: Application) : AndroidViewModel(context) {
    private val _preferences = MutableStateFlow(PrefsModel())

    val preferences: Flow<PrefsModel> = _preferences

    init {
        queryPreferences()
    }

    fun queryPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            _preferences.emit(HmsPushClient.preference)
        }
    }

    fun updatePreference(updateAction: PrefsModel. () -> Unit) {
        val copy = _preferences.value.copy()
        updateAction(copy)
        _preferences.value = copy
        viewModelScope.launch(Dispatchers.IO) {
            HmsPushClient.updatePreference(_preferences.value)
        }
    }

    fun setHmsCoreForeground(foreground: Boolean) {
        HmsCoreUtil.startHmsCoreService(context, foreground)
    }

    fun toggleAppIcon(hide: Boolean) {
        updatePreference { hideAppIcon = hide }
        val newState = if (hide) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, mainActivityAlias),
            newState,
            PackageManager.DONT_KILL_APP
        )
    }
}