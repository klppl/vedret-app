package se.vedret.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.vedret.app.R
import se.vedret.app.data.LocationMode
import se.vedret.app.data.RefreshInterval
import se.vedret.app.data.ThemeMode
import se.vedret.app.ui.theme.LocalVedretExtras

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: WeatherViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            SectionHeader(stringResource(R.string.settings_location_section))
            ChoiceRow(
                label = stringResource(R.string.settings_location_auto),
                description = stringResource(R.string.settings_location_auto_desc),
                selected = state.locationMode == LocationMode.Auto,
                onClick = { vm.setLocationMode(LocationMode.Auto) },
            )
            ChoiceRow(
                label = stringResource(R.string.settings_location_static),
                description = stringResource(R.string.settings_location_static_desc),
                selected = state.locationMode == LocationMode.Static,
                onClick = { vm.setLocationMode(LocationMode.Static) },
            )

            Spacer(Modifier.height(24.dp))
            SectionHeader(stringResource(R.string.settings_refresh_section))
            RefreshInterval.entries.forEach { interval ->
                ChoiceRow(
                    label = stringResource(refreshLabel(interval)),
                    selected = state.autoRefreshMinutes == interval.minutes,
                    onClick = { vm.setAutoRefreshMinutes(interval.minutes) },
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(stringResource(R.string.settings_theme_section))
            ChoiceRow(
                label = stringResource(R.string.theme_rose_pine),
                description = stringResource(R.string.theme_rose_pine_desc),
                selected = state.themeMode == ThemeMode.RosePine,
                onClick = { vm.setThemeMode(ThemeMode.RosePine) },
            )
            ChoiceRow(
                label = stringResource(R.string.theme_rose_pine_dawn),
                description = stringResource(R.string.theme_rose_pine_dawn_desc),
                selected = state.themeMode == ThemeMode.RosePineDawn,
                onClick = { vm.setThemeMode(ThemeMode.RosePineDawn) },
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    val extras = LocalVedretExtras.current
    Text(
        text = text.lowercase(),
        style = MaterialTheme.typography.labelMedium,
        color = extras.muted,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    description: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalVedretExtras.current.muted,
                )
            }
        }
    }
}

private fun refreshLabel(interval: RefreshInterval): Int = when (interval) {
    RefreshInterval.Off -> R.string.refresh_off
    RefreshInterval.Every15 -> R.string.refresh_15
    RefreshInterval.Every30 -> R.string.refresh_30
    RefreshInterval.Every60 -> R.string.refresh_60
    RefreshInterval.Every120 -> R.string.refresh_120
    RefreshInterval.Every240 -> R.string.refresh_240
}
