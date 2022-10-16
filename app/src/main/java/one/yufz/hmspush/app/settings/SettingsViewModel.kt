package one.yufz.hmspush.app.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import one.yufz.hmspush.common.BridgeWrap
import one.yufz.hmspush.common.model.PrefsModel

class SettingsViewModel(val context: Application) : AndroidViewModel(context) {
    private val _preferences = MutableStateFlow(PrefsModel())

    val preferences: Flow<PrefsModel> = _preferences

    init {
        queryPreferences()
    }

    fun queryPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            val model = BridgeWrap.queryPreference(context)
            _preferences.emit(model)
        }
    }

    fun setDisableSignature(disableSignature: Boolean) {
        _preferences.value = _preferences.value.copy(disableSignature = disableSignature)
        viewModelScope.launch(Dispatchers.IO) {
            BridgeWrap.updatePreference(context, _preferences.value)
        }
    }

    fun setGroupMessageById(enable: Boolean) {
        _preferences.value = _preferences.value.copy(groupMessageById = enable)
        viewModelScope.launch(Dispatchers.IO) {
            BridgeWrap.updatePreference(context, _preferences.value)
        }
    }
}