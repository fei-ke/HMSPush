@file:OptIn(ExperimentalAnimationApi::class)

package one.yufz.hmspush.app.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import one.yufz.hmspush.BuildConfig
import one.yufz.hmspush.R
import one.yufz.hmspush.app.HmsPushClient
import one.yufz.hmspush.app.fake.FakeDeviceConfig
import one.yufz.hmspush.app.theme.AppTheme
import one.yufz.hmspush.app.theme.customColors
import one.yufz.hmspush.common.HMS_PACKAGE_NAME
import one.yufz.hmspush.common.HmsCoreUtil
import java.lang.StringBuilder

@Composable
fun AppListScreen(searchText: String, appListViewModel: AppListViewModel = viewModel()) {
    val appList: List<AppInfo> by appListViewModel.appListFlow.collectAsState(initial = emptyList(), Dispatchers.IO)

    appListViewModel.filter(searchText)

    AppList(appList)
}

@Composable
private fun AppList(appList: List<AppInfo>) {
    val bottomPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues()
    LazyColumn(contentPadding = bottomPadding) {
        items(
            items = appList,
            key = { it.packageName }
        ) {
            AppCard(it)
        }
    }
}

@Composable
private fun AppCard(info: AppInfo) {
    val drawable by loadAppIcon(LocalContext.current, info.packageName)
    val drawablePainter = rememberDrawablePainter(drawable)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, end = 4.dp, bottom = 8.dp)
    ) {

        //Icon
        Icon(
            painter = drawablePainter,
            contentDescription = "icon",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(56.dp)
                .padding(all = 8.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {

            //Title
            Text(
                text = info.name,
                color = if (info.registered) MaterialTheme.customColors.active else Color.Unspecified,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            //Subtitle
            Text(
                text = info.packageName,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            //Status
            AppStatus(info)
        }
        Spacer(modifier = Modifier.width(16.dp))

        //More
        var showDropdownMenu by remember { mutableStateOf(false) }
        IconButton(onClick = { showDropdownMenu = true }) {
            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
            MoreDropdownMenu(showDropdownMenu, info, { showDropdownMenu = false })
        }

    }
}

@Composable
private fun FakeDeviceStatus(app: AppInfo) {
    Row {
        Text(
            text = "Zygisk",
            color = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.surfaceVariant),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun loadAppIcon(context: Context, packageName: String): MutableState<Drawable?> {
    val drawable = remember { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(packageName) {
        launch(Dispatchers.IO) {
            drawable.value = context.packageManager.getApplicationIcon(packageName)
        }
    }

    return drawable
}

@Composable
private fun AppStatus(info: AppInfo) {
    val divider = " • "
    val builder = StringBuilder()

    val registerInfo = if (info.registered) stringResource(R.string.registered) else stringResource(R.string.unregistered)
    builder.append(registerInfo)

    if (info.lastPushTime != null) {
        val lastPushInfo = stringResource(R.string.latest_push, DateUtils.getRelativeTimeSpanString(info.lastPushTime))
        builder.append(divider)
        builder.append(lastPushInfo)
    }

    if (info.useZygiskFake) {
        builder.append(divider)
        builder.append("Zygisk")
    }
    Text(
        text = builder.toString(),
        color = if (info.registered) MaterialTheme.customColors.active else Color.Unspecified,
        fontSize = 13.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun MoreDropdownMenu(expanded: Boolean, info: AppInfo, onDismissRequest: () -> Unit) {
    val context = LocalContext.current

    var showUnregisterDialog by remember { mutableStateOf(false) }

    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier = Modifier.requiredWidth(160.dp)) {
        //Launch
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(id = R.string.menu_launch)
                )
            },
            onClick = {
                if (info.packageName == HMS_PACKAGE_NAME) {
                    HmsCoreUtil.startHmsCoreDummyActivity(context)
                } else {
                    Util.launchApp(context, info.packageName)
                }
                onDismissRequest()
            }
        )

        //AppInfo
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(id = R.string.menu_app_info)
                )
            },
            onClick = {
                Util.launchAppInfo(context, info.packageName)
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.clear_hms_notification_channels))
            },
            onClick = {
                HmsPushClient.clearHmsNotificationChannels(info.packageName)
                Toast.makeText(context, R.string.clear_hms_notification_channels_done, Toast.LENGTH_SHORT).show()
                onDismissRequest()
            }
        )
        if (info.useZygiskFake) {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(R.string.menu_enable_zygisk_fake))
                },
                onClick = {
                    onDismissRequest()
                    GlobalScope.launch {
                        FakeDeviceConfig.deleteConfig(info.packageName)
                    }
                }
            )
        } else {
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(R.string.menu_disable_zygisk_fake))
                },
                onClick = {
                    onDismissRequest()
                    GlobalScope.launch {
                        FakeDeviceConfig.update(info.packageName, emptyList())
                    }
                }
            )
        }
        //Unregister
        if (info.registered) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.menu_unregister),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    showUnregisterDialog = true
                    onDismissRequest()
                }
            )
        }
    }
    if (showUnregisterDialog) {
        UnregisterDialog(
            info = info,
            onDismissRequest = {
                showUnregisterDialog = false
            }
        )
    }
}

@Composable
private fun UnregisterDialog(info: AppInfo, onDismissRequest: () -> Unit, viewModel: AppListViewModel = viewModel()) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.dialog_confirm_unregister))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.unregisterPush(info.packageName)
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(id = R.string.dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        }
    )
}


@Preview
@Composable
private fun Preview() {
    val appInfo = AppInfo(
        packageName = BuildConfig.APPLICATION_ID, lastPushTime = System.currentTimeMillis(), name = stringResource(R.string.app_name), registered = true
    )
    val list = listOf(appInfo, appInfo.copy(registered = false))
    AppTheme() {
        AppList(list)
    }
}