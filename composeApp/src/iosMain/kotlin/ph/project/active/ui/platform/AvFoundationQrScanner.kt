package ph.project.active.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberQrScanner(): QrScanner = remember { AvFoundationQrScanner() }

@OptIn(ExperimentalForeignApi::class)
private class AvFoundationQrScanner : QrScanner {
    override val hasTorch: Boolean
        get() = false

    @Composable
    override fun Preview(torchOn: Boolean, onText: (String) -> Unit, modifier: Modifier) {
        val onTextState = rememberUpdatedState(onText)
        val session = remember { AVCaptureSession() }
        val handed = remember { HandedOff() }
        DisposableEffect(session) {
            val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, error = null) }
            if (input != null && session.canAddInput(input)) session.addInput(input)
            val output = AVCaptureMetadataOutput()
            if (session.canAddOutput(output)) {
                session.addOutput(output)
                output.setMetadataObjectsDelegate(
                    QrDelegate { text ->
                        if (!handed.taken) {
                            handed.taken = true
                            onTextState.value(text)
                        }
                    },
                    dispatch_get_main_queue(),
                )
                output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
            }
            session.startRunning()
            onDispose { session.stopRunning() }
        }
        UIKitView(
            factory = {
                val view = UIView()
                val layer = AVCaptureVideoPreviewLayer(session = session)
                layer.videoGravity = AVLayerVideoGravityResizeAspectFill
                view.layer.addSublayer(layer)
                view
            },
            modifier = modifier,
            update = { view ->
                val layer = view.layer.sublayers?.firstOrNull() as? AVCaptureVideoPreviewLayer
                CATransaction.begin()
                CATransaction.setDisableActions(true)
                layer?.setFrame(view.bounds)
                CATransaction.commit()
            },
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private class HandedOff {
    var taken: Boolean = false
}

private class QrDelegate(private val onText: (String) -> Unit) : NSObject(), platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol {
    override fun captureOutput(
        output: platform.AVFoundation.AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: platform.AVFoundation.AVCaptureConnection,
    ) {
        val text = didOutputMetadataObjects
            .filterIsInstance<AVMetadataMachineReadableCodeObject>()
            .firstOrNull()
            ?.stringValue
        if (text != null) onText(text)
    }
}
