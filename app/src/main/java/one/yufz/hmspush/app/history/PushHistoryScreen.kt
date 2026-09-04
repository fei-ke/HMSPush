@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalComposeUiApi::class,
)

package one.yufz.hmspush.app.history

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.mvrx.compose.collectAsState
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import one.yufz.hmspush.R
import one.yufz.hmspush.app.nav.LocalNavigator
import one.yufz.hmspush.app.widget.SearchBar
import one.yufz.hmspush.app.workaround.mavericksViewModel
import one.yufz.hmspush.common.model.PushRecordModel

@Composable
fun PushHistoryScreen(
    packageName: String? = null,
    viewModel: PushHistoryViewModel = mavericksViewModel()
) {
    val navigator = LocalNavigator.current
    val state by viewModel.collectAsState()
    var searching by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    //从单个应用的菜单进来时预置过滤词
    LaunchedEffect(packageName) {
        if (packageName != null) {
            viewModel.setPackageFilter(packageName)
        }
    }

    //滚动到底部自动加载下一页
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisible ->
                val total = listState.layoutInfo.totalItemsCount
                if (lastVisible != null && total > 0 && lastVisible >= total - 10) {
                    viewModel.loadMore()
                }
            }
    }

    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    if (!searching) {
                        Column {
                            Text(text = stringResource(id = R.string.push_history))
                            if (state.totalCount > 0) {
                                Text(
                                    text = stringResource(R.string.push_history_count, state.totalCount),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (!searching) {
                        IconButton(onClick = { searching = true }) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                        }
                    } else {
                        SearchBar(
                            searchText = state.filterKeywords,
                            placeholderText = stringResource(id = R.string.menu_search),
                            onNavigateBack = {
                                searching = false
                                viewModel.filter("")
                            },
                            onSearchTextChanged = { viewModel.filter(it) }
                        )
                    }
                    MoreMenu(
                        onExport = { viewModel.export() },
                        onClear = { showClearDialog = true },
                        onRefresh = { viewModel.refresh() },
                    )
                }
            )
        },
        modifier = Modifier.nestedScroll(TopAppBarDefaults.pinnedScrollBehavior().nestedScrollConnection),
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val records = state.filteredRecords

            if (state.loading && records.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (records.isEmpty()) {
                EmptyHint(state.recordFilePath)
            } else {
                LazyColumn(state = listState) {
                    itemsIndexed(records) { index, record ->
                        RecordRow(record)
                        if (index < records.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                    if (state.loadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        ClearConfirmDialog(
            onConfirm = {
                viewModel.clearRecords()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }

    state.exportResult?.let {
        ExportResultDialog(it) { viewModel.dismissExportResult() }
    }
}

@Composable
private fun RecordRow(record: PushRecordModel) {
    val context = LocalContext.current
    val drawable by loadAppIcon(context, record.packageName)
    val appLabel = remember(record.packageName) { loadAppLabel(context, record.packageName) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 10.dp)
    ) {
        Icon(
            painter = rememberDrawablePainter(drawable),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            //应用名 + 相对时间
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = appLabel,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = DateUtils.getRelativeTimeSpanString(record.pushTime).toString(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            //标题
            record.title?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = it, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            //正文
            record.content?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            //包名 · id · channel
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildString {
                    append(record.packageName)
                    append(" · id=")
                    append(record.notifyId)
                    record.channelId?.takeIf { it.isNotBlank() }?.let {
                        append(" · ")
                        append(it)
                    }
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MoreMenu(onExport: () -> Unit, onClear: () -> Unit, onRefresh: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.requiredWidth(160.dp)
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.push_history_refresh)) },
                onClick = {
                    onRefresh()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.push_history_export)) },
                onClick = {
                    onExport()
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.push_history_clear),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    onClear()
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun EmptyHint(filePath: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.push_history_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (filePath.isNotEmpty()) {
                Text(
                    text = filePath,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ClearConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.push_history_clear)) },
        text = { Text(text = stringResource(id = R.string.push_history_clear_confirm)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(id = R.string.dialog_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        }
    )
}

@Composable
private fun ExportResultDialog(result: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.push_history_export)) },
        text = { Text(text = result) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.dialog_confirm))
            }
        }
    )
}

@Composable
private fun loadAppIcon(context: Context, packageName: String): MutableState<Drawable?> {
    val drawable = remember(packageName) { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(packageName) {
        launch(Dispatchers.IO) {
            drawable.value = try {
                context.packageManager.getApplicationIcon(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    return drawable
}

private fun loadAppLabel(context: Context, packageName: String): String {
    return try {
        val pm = context.packageManager
        pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }
}
