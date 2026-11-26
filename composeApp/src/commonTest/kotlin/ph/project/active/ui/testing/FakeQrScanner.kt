package ph.project.active.ui.testing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import ph.project.active.ui.platform.QrScanner

/**
 * Flow 7 spec section 8: the camera stand-in. [emit] hands [text] to the scanner screen as if the camera
 * had read it. [torchOn] is what the screen last asked for. The preview is a dark box tagged `fake_qr_preview`.
 */
class FakeQrScanner(override val hasTorch: Boolean = false) : QrScanner {
    var torchOn by mutableStateOf(false)
        private set
    private var onText: ((String) -> Unit)? = null

    @Composable
    override fun Preview(torchOn: Boolean, onText: (String) -> Unit, modifier: Modifier) {
        SideEffect {
            this.torchOn = torchOn
            this.onText = onText
        }
        DisposableEffect(Unit) {
            onDispose { this@FakeQrScanner.onText = null }
        }
        Box(modifier.background(Color.Black).testTag("fake_qr_preview"))
    }

    /** Sends [text] to the screen that shows the preview now. Does nothing while no preview is shown. */
    fun emit(text: String) {
        onText?.invoke(text)
    }
}
