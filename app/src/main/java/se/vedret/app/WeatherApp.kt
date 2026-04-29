package se.vedret.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import se.vedret.app.ui.SettingsScreen
import se.vedret.app.ui.WeatherScreen
import se.vedret.app.ui.WeatherViewModel
import se.vedret.app.ui.theme.VedretTheme

private enum class Screen { Weather, Settings }

@Composable
fun WeatherApp() {
    val vm: WeatherViewModel = viewModel(factory = WeatherViewModel.Factory)
    val state by vm.state.collectAsStateWithLifecycle()
    VedretTheme(mode = state.themeMode) {
        var screen by rememberSaveable { mutableStateOf(Screen.Weather) }
        when (screen) {
            Screen.Weather -> WeatherScreen(
                vm = vm,
                onOpenSettings = { screen = Screen.Settings },
            )
            Screen.Settings -> SettingsScreen(
                vm = vm,
                onBack = { screen = Screen.Weather },
            )
        }
    }
}
