package one.yufz.hmspush.app.home

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import one.yufz.hmspush.BuildConfig
import one.yufz.hmspush.R
import one.yufz.hmspush.app.HmsPushClient
import one.yufz.hmspush.common.HMS_PACKAGE_NAME

class HomeViewModel(val app: Application) : AndroidViewModel(app) {
    enum class Reason {
        None,
        Checking,
        HmsCoreNotInstalled,
        HmsCoreNotActivated,
        HmsPushVersionNotMatch
    }

    data class UiState(val usable: Boolean, val tips: String, val reason: Reason)

    private val _uiState = MutableStateFlow(UiState(false, "", Reason.Checking))
    val uiState: StateFlow<UiState> = _uiState

    private var _searchState = MutableStateFlow(false)
    val searchState: Flow<Boolean> = _searchState

    private var _searchText = MutableStateFlow("")
    val searchText: Flow<String> = _searchText

    fun setSearching(searching: Boolean) {
        _searchState.value = searching
    }

    fun checkHmsCore() {
        try {
            app.packageManager.getApplicationInfo(HMS_PACKAGE_NAME, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            _uiState.value = UiState(false, app.getString(R.string.hms_core_not_found), Reason.HmsCoreNotInstalled)
            return
        }

        val moduleVersion = HmsPushClient.moduleVersion
        if (moduleVersion == null) {
            _uiState.value = UiState(false, app.getString(R.string.hms_not_activated), Reason.HmsCoreNotActivated)
            return
        }

        if (moduleVersion.versionName != BuildConfig.VERSION_NAME) {
            _uiState.value = UiState(false, app.getString(R.string.hms_not_activated), Reason.HmsPushVersionNotMatch)
            return
        }

        _uiState.value = UiState(true, "", Reason.None)
    }

    fun setSearchText(text: String) {
        _searchText.value = text
    }
}