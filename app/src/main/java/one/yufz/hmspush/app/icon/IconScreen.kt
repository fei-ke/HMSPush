@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalComposeUiApi::class,
)

package one.yufz.hmspush.app.icon

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import de.charlex.compose.HtmlText
import one.yufz.hmspush.R
import one.yufz.hmspush.app.LocalNavHostController
import one.yufz.hmspush.app.widget.LoadingDialog
import one.yufz.hmspush.app.widget.SearchBar

@Composable
fun IconScreen(iconViewModel: IconViewModel = viewModel()) {
    val navHostController = LocalNavHostController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.notification_icon))
                },
                actions = {
                    var searching by remember { mutableStateOf(false) }

                    if (!searching) {
                        IconButton(onClick = { searching = true }) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                        }
                    } else {
                        SearchBar(
                            placeholderText = stringResource(id = R.string.menu_search),
                            onNavigateBack = { searching = false },
                            onSearchTextChanged = { iconViewModel.filter(it) }
                        )
                    }

                    var showMoreMenu by remember { mutableStateOf(false) }

                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                    }

                    MoreMenu(showMoreMenu, onDismissRequest = { showMoreMenu = false })
                },
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val icons by iconViewModel.iconsFlow.collectAsState(initial = emptyList())

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(icons) {
                    Row(
                        Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (it.iconColor != null) Color(it.iconColor!!) else MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier
                                .padding(8.dp)
                        ) {
                            //App Icon
                            Icon(
                                bitmap = it.iconBitmap.asImageBitmap(),
                                contentDescription = it.appName,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(16.dp),
                                tint = Color.White,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                //small Icon
                                Icon(
                                    bitmap = it.iconBitmap.asImageBitmap(),
                                    contentDescription = it.appName,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp),
                                    tint = LocalContentColor.current,
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                //App name
                                Text(
                                    text = it.appName,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                )
                            }

                            //Package name
                            Text(
                                text = it.packageName,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            //contributors
                            Row {
                                Text(
                                    text = stringResource(id = R.string.icon_contributors),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                it.contributorName?.let { names ->
                                    Text(
                                        text = names,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { iconViewModel.deleteIcon(it.packageName) }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Delete")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }

        val importState by iconViewModel.importState.collectAsState()
        if (importState.info != null) {
            InfoDialog(importState.info!!) {
                iconViewModel.cancelImport()
            }
        } else if (importState.loading) {
            LoadingDialog(
                onDismissRequest = { iconViewModel.cancelImport() },
                text = stringResource(R.string.importing)
            )
        }
    }
}

@Composable
fun MoreMenu(showMoreMenu: Boolean, onDismissRequest: () -> Unit) {
    var showImportDialog by remember { mutableStateOf(false) }
    val iconViewModel: IconViewModel = viewModel()
    DropdownMenu(expanded = showMoreMenu, onDismissRequest = onDismissRequest, modifier = Modifier.defaultMinSize(minWidth = 160.dp)) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.import_from_url))
            },
            onClick = {
                showImportDialog = true
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.clear_icons))
            },
            onClick = {
                iconViewModel.clearIcons()
                onDismissRequest()
            }
        )
    }

    if (showImportDialog) {
        ImportFromUrlDialog(
            onConfirm = {
                iconViewModel.fetchIconFromUrl(it)
            },
            onDismissRequest = {
                showImportDialog = false
            }
        )
    }
}

@Composable
fun InfoDialog(info: String, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Text(text = info)
        },
        confirmButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(id = R.string.dialog_confirm))
            }
        }
    )
}

@Composable
fun ImportFromUrlDialog(onConfirm: (String) -> Unit, onDismissRequest: () -> Unit) {
    var textState by remember { mutableStateOf(TextFieldValue(IconViewModel.ICON_URL)) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.import_dialog_title))
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                HtmlText(
                    text = stringResource(id = R.string.icon_library_notice),
                    color = LocalContentColor.current,
                    urlSpanStyle = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textState,
                    onValueChange = {
                        textState = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(textState.text)
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        }
    )
}