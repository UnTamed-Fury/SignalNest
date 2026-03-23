package fury.signalnest.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fury.signalnest.app.ui.screens.MainScaffold
import fury.signalnest.app.ui.screens.OnboardingScreen
import fury.signalnest.app.ui.theme.SignalNestTheme
import fury.signalnest.app.ui.viewmodels.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val theme    by settingsVm.theme.collectAsStateWithLifecycle()
            val amoled   by settingsVm.amoled.collectAsStateWithLifecycle()
            val onboarded by settingsVm.onboarded.collectAsStateWithLifecycle()

            SignalNestTheme(theme, amoled) {
                if (onboarded) {
                    MainScaffold(settingsVm)
                } else {
                    OnboardingScreen()
                }
            }
        }
    }
}
