package one.yufz.hmspush.app.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RemoveModerator
import androidx.compose.material.icons.outlined.Upgrade
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import one.yufz.hmspush.R
import one.yufz.hmspush.app.LocalNavHostController
import one.yufz.hmspush.common.model.PrefsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val navHostController = LocalNavHostController.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navHostController.popBackStack()
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.menu_settings))
                }
            )
        }) { paddingValues ->

        val preferences by viewModel.preferences.collectAsState(PrefsModel())

        Surface(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                SwitchPreference(
                    title = stringResource(id = R.string.disable_signature),
                    summary = stringResource(id = R.string.disable_signature_summary),
                    icon = Icons.Outlined.RemoveModerator,
                    checked = preferences.disableSignature,
                    onCheckedChange = {
                        viewModel.updatePreference { disableSignature = it }
                    }
                )
                SwitchPreference(
                    title = stringResource(id = R.string.keep_history_message),
                    summary = stringResource(id = R.string.keep_history_message_summary),
                    icon = Icons.Outlined.FormatListBulleted,
                    checked = preferences.groupMessageById,
                    onCheckedChange = {
                        viewModel.updatePreference { groupMessageById = it }
                    }
                )
                SwitchPreference(
                    title = stringResource(id = R.string.keep_alive),
                    summary = stringResource(id = R.string.keep_alive_summary),
                    icon = Icons.Outlined.Computer,
                    checked = preferences.keepAlive,
                    onCheckedChange = {
                        viewModel.updatePreference { keepAlive = it }
                        viewModel.setHmsCoreForeground(it)
                    }
                )
                SwitchPreference(
                    title = stringResource(id = R.string.notification_icon),
                    icon = Icons.Outlined.Palette,
                    checked = preferences.useCustomIcon,
                    showDivider = true,
                    onCheckedChange = {
                        viewModel.updatePreference { useCustomIcon = it }
                    },
                    onClick = {
                        navHostController.navigate("icon")
                    }
                )
                if (preferences.useCustomIcon) {
                    SwitchPreference(
                        title = stringResource(id = R.string.tint_notification_icon),
                        summary = stringResource(id = R.string.tint_notification_icon_summary),
                        icon = Icons.Outlined.FormatColorFill,
                        checked = preferences.tintIconColor,
                        onCheckedChange = {
                            viewModel.updatePreference { tintIconColor = it }
                        }
                    )
                }
                SwitchPreference(
                    title = stringResource(id = R.string.hide_app_icon),
                    icon = Icons.Outlined.Palette,
                    checked = preferences.hideAppIcon,
                    showDivider = true,
                    onCheckedChange = viewModel::toggleAppIcon,
                )
            }

        }
    }
}

@Composable
fun SwitchPreference(
    title: String, summary: String? = null, icon: ImageVector?, checked: Boolean,
    showDivider: Boolean = false,
    onCheckedChange: (checked: Boolean) -> Unit,
    onClick: () -> Unit = { onCheckedChange(!checked) }
) {
    Preference(
        title = title,
        summary = summary,
        icon = icon,
        showDivider = showDivider,
        action = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        onClick = onClick
    )
}

@Composable
fun Preference(title: String, summary: String? = null, icon: ImageVector?, showDivider: Boolean = false, action: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Row(
            Modifier
                .defaultMinSize(minHeight = 64.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .align(alignment = Alignment.CenterVertically)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleLarge
                )
                if (summary != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (action != null) {
                if (showDivider) {
                    Divider(
                        Modifier
                            .padding(horizontal = 8.dp)
                            .width(1.dp)
                            .height(32.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                action()
            }
        }
    }
}

@Preview
@Composable
fun PreviewSettings() {
    SwitchPreference("title", "summary",
        Icons.Filled.Android,
        checked = false,
        showDivider = true,
        onCheckedChange = {}
    )
}