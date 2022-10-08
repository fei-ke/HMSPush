package one.yufz.hmspush.app.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.RemoveModerator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import one.yufz.hmspush.R
import one.yufz.hmspush.app.LocalNavHostController
import one.yufz.hmspush.common.BridgeWrap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
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
        Surface(modifier = Modifier.padding(paddingValues)) {
            Box() {
                Column {
                    val context = LocalContext.current
                    var checked by remember { mutableStateOf(BridgeWrap.isDisableSignature(context)) }
                    Surface(modifier = Modifier.clickable {
                        checked = checked.not()
                    }) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RemoveModerator,
                                contentDescription = null,
                                modifier = Modifier.align(alignment = Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = stringResource(id = R.string.disable_signature))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(id = R.string.disable_signature_summary),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(checked = checked, onCheckedChange = {
                                checked = it
                                BridgeWrap.setDisableSignature(context, it)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewSettings() {
    SettingsScreen()
}