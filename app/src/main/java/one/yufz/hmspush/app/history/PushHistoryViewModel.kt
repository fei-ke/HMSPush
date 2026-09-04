package one.yufz.hmspush.app.history

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.withState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import one.yufz.hmspush.app.App
import one.yufz.hmspush.app.HmsPushClient
import one.yufz.hmspush.common.model.PushRecordModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PushHistoryState(
    val records: List<PushRecordModel> = emptyList(),
    val totalCount: Int = 0,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val reachedEnd: Boolean = false,
    val filterKeywords: String = "",
    val packageFilter: String? = null,
    val recordFilePath: String = "",
    val exportResult: String? = null,
) : MavericksState {
    val filteredRecords: List<PushRecordModel>
        get() = if (filterKeywords.isEmpty()) records else records.filter {
            it.packageName.contains(filterKeywords, true)
                    || it.title?.contains(filterKeywords, true) == true
                    || it.content?.contains(filterKeywords, true) == true
        }
}

class PushHistoryViewModel(initialState: PushHistoryState) : MavericksViewModel<PushHistoryState>(initialState) {
    companion object {
        private const val PAGE_SIZE = 100

        //单次 Binder 事务上限 1MB，按每条记录数百字节估算留足余量
        private const val MAX_RELOAD_SIZE = 1000
    }

    private val app = App.instance

    init {
        refresh()

        //推送到达时自动刷新首屏
        HmsPushClient.getPushRecordChangeFlow()
            .onEach { refresh() }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        withState { state ->
            //已经翻过页时保持同样多的记录，避免新推送到达把列表缩回第一页
            val loadSize = state.records.size.coerceIn(PAGE_SIZE, MAX_RELOAD_SIZE)

            viewModelScope.launch(Dispatchers.IO) {
                setState { copy(loading = true) }

                val filePath = HmsPushClient.getPushRecordFilePath()
                val total = HmsPushClient.getPushRecordCount()
                val records = HmsPushClient.getPushRecords(loadSize, 0)

                setState {
                    copy(
                        records = records,
                        totalCount = total,
                        recordFilePath = filePath,
                        loading = false,
                        reachedEnd = records.size < loadSize,
                    )
                }
            }
        }
    }

    fun loadMore() {
        withState { state ->
            if (state.loading || state.loadingMore || state.reachedEnd) return@withState

            viewModelScope.launch(Dispatchers.IO) {
                setState { copy(loadingMore = true) }

                val offset = state.records.size
                val page = HmsPushClient.getPushRecords(PAGE_SIZE, offset)

                setState {
                    copy(
                        records = records + page,
                        loadingMore = false,
                        reachedEnd = page.size < PAGE_SIZE,
                    )
                }
            }
        }
    }

    fun filter(keywords: String) {
        setState { copy(filterKeywords = keywords) }
    }

    fun setPackageFilter(packageName: String?) {
        setState { copy(packageFilter = packageName, filterKeywords = packageName ?: "") }
    }

    fun clearRecords() {
        viewModelScope.launch(Dispatchers.IO) {
            HmsPushClient.clearPushRecords()
            refresh()
        }
    }

    fun dismissExportResult() {
        setState { copy(exportResult = null) }
    }

    /**
     * 导出到 Download 目录。走 MediaStore，不需要存储权限。
     * 记录文件本身在 HMS Core 的外部私有目录下，卸载 HMS Core 会被删，导出是唯一的长期保全手段。
     */
    fun export() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val sourcePath = HmsPushClient.getPushRecordFilePath()
                require(sourcePath.isNotEmpty()) { "record file path is empty" }

                val source = File(sourcePath)
                require(source.exists()) { "record file does not exist" }

                val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                val fileName = "HMSPush-history-$stamp.jsonl"

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = app.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("failed to create file in Download")

                resolver.openOutputStream(uri)?.use { output ->
                    source.inputStream().use { it.copyTo(output) }
                } ?: error("failed to open output stream")

                "Download/$fileName"
            }

            setState {
                copy(
                    exportResult = result.fold(
                        onSuccess = { it },
                        onFailure = { it.message ?: it.javaClass.simpleName }
                    )
                )
            }
        }
    }
}
