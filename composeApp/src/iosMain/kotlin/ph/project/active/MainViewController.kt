package ph.project.active

import androidx.compose.ui.window.ComposeUIViewController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import ph.project.active.di.initKoin
import ph.project.active.ui.TrxApp
import platform.UIKit.UIViewController

@OptIn(ExperimentalNativeApi::class)
fun MainViewController(): UIViewController {
    initKoin()
    if (Platform.isDebugBinary) installDebugSeed()
    return ComposeUIViewController { TrxApp() }
}
