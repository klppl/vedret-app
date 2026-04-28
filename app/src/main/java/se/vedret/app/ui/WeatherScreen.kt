package se.vedret.app.ui

import android.Manifest
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import se.vedret.app.R
import se.vedret.app.ui.components.DayCard
import se.vedret.app.ui.components.Hero
import se.vedret.app.ui.components.SearchPill
import se.vedret.app.ui.components.SourcesFooter
import se.vedret.app.ui.components.Suggestions
import se.vedret.app.ui.components.TodayRow

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    vm: WeatherViewModel,
    onOpenSettings: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var searchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    val locationPerm = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
    var pendingGps by remember { mutableStateOf(false) }

    LaunchedEffect(locationPerm.status) {
        if (!pendingGps) return@LaunchedEffect
        when (locationPerm.status) {
            is PermissionStatus.Granted -> {
                pendingGps = false
                vm.requestLocation()
            }
            is PermissionStatus.Denied -> {
                pendingGps = false
            }
        }
    }

    fun dismiss() {
        keyboard?.hide()
        focusManager.clearFocus()
    }

    fun useLocation() {
        if (locationPerm.status is PermissionStatus.Granted) {
            vm.requestLocation()
        } else {
            pendingGps = true
            locationPerm.launchPermissionRequest()
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { dismiss() })
                },
        ) {
            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = vm::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(Modifier.height(16.dp))
                    SearchPill(
                        value = state.query,
                        searching = state.loading || state.refreshing,
                        onValueChange = vm::onQueryChange,
                        onSubmit = { name ->
                            vm.submitCity(name)
                            dismiss()
                        },
                        onUseLocation = {
                            dismiss()
                            useLocation()
                        },
                        onFocusChange = { searchFocused = it },
                    )
                    if (searchFocused && state.suggestions.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Suggestions(
                            items = state.suggestions,
                            onPick = { name ->
                                vm.submitCity(name)
                                dismiss()
                            },
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    val data = state.data
                    val err = state.error ?: data?.error?.takeIf { it.isNotEmpty() }
                    when {
                        err != null -> Box(
                            Modifier.fillMaxWidth().padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) { Text(err.lowercase(), textAlign = TextAlign.Center) }
                        data == null -> Box(
                            Modifier.fillMaxWidth().padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) { Text("…", textAlign = TextAlign.Center) }
                        else -> {
                            Hero(current = data.current)
                            Spacer(Modifier.height(24.dp))
                            if (data.upcoming.isNotEmpty()) {
                                TodayRow(slots = data.upcoming)
                                Spacer(Modifier.height(24.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                data.days.forEach { DayCard(day = it) }
                            }
                            Spacer(Modifier.height(24.dp))
                            SettingsLink(onClick = onOpenSettings)
                            Spacer(Modifier.height(16.dp))
                            SourcesFooter(providers = data.providers)
                            Spacer(Modifier.height(48.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsLink(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(imageVector = Icons.Default.Settings, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.settings_title))
    }
}

