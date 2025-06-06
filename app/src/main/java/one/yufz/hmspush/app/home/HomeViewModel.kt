package one.yufz.hmspush.app.home

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import one.yufz.hmspush.R
import one.yufz.hmspush.app.HmsPushClient
import one.yufz.hmspush.app.util.registerPackageChangeFlow
import one.yufz.hmspush.common.API_VERSION
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

    private var registerJob: Job? = null

    init {
        app.registerPackageChangeFlow()
            .filter { it.dataString?.removePrefix("package:") == HMS_PACKAGE_NAME }
            .onEach { onHmsPackageChanged(it) }
            .launchIn(viewModelScope)

        registerServiceChange()
    }

    private fun onHmsPackageChanged(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> registerServiceChange()
            Intent.ACTION_PACKAGE_REMOVED -> registerJob?.cancel()
        }
        checkHmsCore()
    }

    private fun registerServiceChange() {
        registerJob?.cancel()
        registerJob = HmsPushClient.getHmsPushServiceFlow()
            .onEach { checkHmsCore() }
            .launchIn(viewModelScope)
    }

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

        if (moduleVersion.apiVersion != API_VERSION) {
            _uiState.value = UiState(false, app.getString(R.string.hms_version_not_match), Reason.HmsPushVersionNotMatch)
            return
        }

        _uiState.value = UiState(true, "", Reason.None)
    }

    fun setSearchText(text: String) {
        _searchText.value = text
    }
}
