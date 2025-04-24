package one.yufz.hmspush.app.icon

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.yufz.hmspush.R
import one.yufz.hmspush.app.HmsPushClient
import one.yufz.hmspush.common.IconData
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class IconViewModel(val app: Application) : AndroidViewModel(app) {
    companion object {
        const val ICON_URL = "https://raw.githubusercontent.com/fankes/AndroidNotifyIconAdapt/main/APP/NotifyIconsSupportConfig.json"
    }

    data class ImportState(val loading: Boolean, val info: String? = null)

    private val _iconsFlow = MutableStateFlow<List<IconData>>(emptyList())

    private val _importState = MutableStateFlow<ImportState>(ImportState(false))
    val importState: StateFlow<ImportState> = _importState

    private val filterKeywords = MutableStateFlow("")

    val iconsFlow: Flow<List<IconData>> = _iconsFlow.combine(filterKeywords) { list, keywords ->
        if (keywords.isEmpty()) return@combine list

        list.filter { it.appName.contains(keywords, true) || it.packageName.contains(keywords, true) }
    }

    init {
        loadIcon()
    }

    fun fetchIconFromUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _importState.emit(ImportState(true))

            try {
                readIconFromUrl(url).forEach {
                    HmsPushClient.saveIcon(it)
                }
            } catch (e: Throwable) {
                _importState.emit(ImportState(false, e.message))
                return@launch
            }

            _importState.emit(ImportState(false, getApplication<Application>().getString(R.string.import_complete)))

            loadIcon()
        }
    }

    fun loadIcon() {
        viewModelScope.launch(Dispatchers.IO) {
            _iconsFlow.value = HmsPushClient.allIcon.mapNotNull { it.toIconData() }
        }
    }

    private suspend fun readIconFromUrl(url: String): List<IconData> {
        return withContext(Dispatchers.IO) {
            val jsonString = URL(url).readText()
            val jsonArray = JSONArray(jsonString)
            val iconList = ArrayList<IconData>(jsonArray.length())

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.get(i) as JSONObject
                iconList.add(IconData.fromJson(obj))
            }
            iconList
        }
    }

    fun cancelImport() {
        _importState.value = ImportState(false)
    }

    fun filter(keywords: String) {
        viewModelScope.launch {
            filterKeywords.emit(keywords)
        }
    }

    fun clearIcons() {
        viewModelScope.launch(Dispatchers.IO) {
            HmsPushClient.deleteIcon()
            loadIcon()
        }
    }

    fun deleteIcon(vararg packageName: String) {
        viewModelScope.launch {
            val set = packageName.toHashSet()
            _iconsFlow.value = _iconsFlow.value.filterNot { it.packageName in set }

            HmsPushClient.deleteIcon(*packageName)
        }
    }
}
