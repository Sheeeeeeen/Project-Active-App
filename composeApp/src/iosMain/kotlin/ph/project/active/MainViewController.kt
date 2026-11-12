package ph.project.active

import androidx.compose.ui.window.ComposeUIViewController
import ph.project.active.di.initKoin
import ph.project.active.ui.TrxApp
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { TrxApp() }
}
