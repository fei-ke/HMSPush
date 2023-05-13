package one.yufz.hmspush.app.fake

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import one.yufz.hmspush.app.hms.SupportHmsAppList

data class AppConfig(val name: String, val packageName: String, val enabled: Boolean)

data class UIState(val configList: List<AppConfig>, val filterKeywords: String = "") {
    val filteredConfigList: List<AppConfig> = if (filterKeywords.isEmpty()) configList else
        configList.filter { it.name.contains(filterKeywords, true) || it.packageName.contains(filterKeywords, true) }
}

class FakeDeviceViewModel(val app: Application) : AndroidViewModel(app) {
    companion object {
        private const val TAG = "FakeDeviceViewModel"
    }

    private val _uiState = MutableStateFlow<UIState>(UIState(emptyList()))
    val uiState = _uiState

    private val supportedAppList = SupportHmsAppList(app)

    private val fakeDeviceConfig = FakeDeviceConfig

    init {
        viewModelScope.launch {
            fakeDeviceConfig.loadConfig()
            supportedAppList.init()
        }

        viewModelScope.launch(Dispatchers.IO) {
            combine(supportedAppList.appListFlow, fakeDeviceConfig.configMapFlow, ::mergeSource).collect { list ->
                _uiState.update { old ->
                    val appConfigs = if (old.configList.isNotEmpty()) {
                        mergeConfigList(old.configList, list)
                    } else {
                        list.sortedByDescending { it.enabled }
                    }
                    old.copy(configList = appConfigs)
                }
            }
        }
        load()
    }

    /**
     * Merge the current config list and the new config list with a stable order.
     */
    private fun mergeConfigList(current: List<AppConfig>, newList: List<AppConfig>): List<AppConfig> {
        val newConfigMap = newList.associateBy { it.packageName }.toMutableMap()
        val merged = current.map { old ->
            newConfigMap.remove(old.packageName) ?: old
        }
        return merged + newConfigMap.values
    }

    private fun mergeSource(supportList: List<String>, configs: ConfigMap): List<AppConfig> {
        Log.d(TAG, "mergeSource() called with: supportList = $supportList, configs = $configs")

        return supportList.map { pkg ->
            AppConfig(loadName(pkg), pkg, configs.contains(pkg))
        }
    }

    private fun loadName(packageName: String): String {
        return try {
            app.packageManager.getApplicationInfo(packageName, 0).loadLabel(app.packageManager).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    fun load() {
        viewModelScope.launch {
            fakeDeviceConfig.loadConfig()
        }
    }

    fun update(appConfig: AppConfig) {
        viewModelScope.launch {
            if (appConfig.enabled) {
                fakeDeviceConfig.update(appConfig.packageName, emptyList())
            } else {
                fakeDeviceConfig.deleteConfig(appConfig.packageName)
            }
        }
    }

    fun filter(filter: String) {
        _uiState.update {
            it.copy(filterKeywords = filter)
        }
    }
}