@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalComposeUiApi::class,
)

package one.yufz.hmspush.app.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import one.yufz.hmspush.R
import one.yufz.hmspush.app.LocalNavHostController
import one.yufz.hmspush.app.widget.LifecycleAware
import one.yufz.hmspush.app.widget.SearchBar

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by homeViewModel.uiState.collectAsState()

    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = { AppBar(scrollBehavior, uiState.usable, onSearchTextChanged = { searchText = it }) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LifecycleAware(onStart = { homeViewModel.checkHmsCore() }) {
                if (uiState.usable) {
                    AppListScreen(searchText)
                } else if (uiState.reason == HomeViewModel.Reason.Checking) {
                    Loading()
                } else {
                    HmsCoreTips(uiState)
                }
            }
        }
    }
}

@Composable
private fun AppBar(scrollBehavior: TopAppBarScrollBehavior, withSearch: Boolean, onSearchTextChanged: (String) -> Unit) {
    var searching by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        scrollBehavior = scrollBehavior,
        actions = {
            //Search
            if (withSearch) {
                if (!searching) {
                    IconButton(onClick = { searching = true }) {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                    }
                } else {
                    SearchBar(
                        placeholderText = stringResource(id = R.string.menu_search),
                        onNavigateBack = { searching = false },
                        onSearchTextChanged = onSearchTextChanged
                    )
                }
            }
            //More
            AppBarMoreMenu()
        }
    )
}

@Composable
private fun AppBarMoreMenu() {
    //More
    var openMoreMenu by remember { mutableStateOf(false) }
    IconButton(onClick = { openMoreMenu = true }) {
        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
        if (openMoreMenu) {
            DropdownMenu(
                expanded = openMoreMenu,
                onDismissRequest = { openMoreMenu = false },
                modifier = Modifier.requiredWidth(160.dp)
            ) {
                val navController = LocalNavHostController.current
                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(id = R.string.menu_settings))
                    },
                    onClick = {
                        navController.navigate("settings")
                        openMoreMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun Loading() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun HmsCoreTips(uiState: HomeViewModel.UiState) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(all = 16.dp)
                .fillMaxWidth()
                .requiredHeight(80.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(text = uiState.tips, Modifier.align(Alignment.Center))
            }
        }
    }
}