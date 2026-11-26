package ph.project.active.ui.platform

import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import ph.project.active.presentation.attend.looksLikeAttendanceCode

@Composable
actual fun rememberQrScanner(): QrScanner {
    val context = LocalContext.current
    return remember(context) { CameraXQrScanner(context.applicationContext) }
}

private class CameraXQrScanner(context: Context) : QrScanner {
    override val hasTorch: Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    @Composable
    override fun Preview(torchOn: Boolean, onText: (String) -> Unit, modifier: Modifier) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val onTextState = rememberUpdatedState(onText)
        val handed = remember { AtomicBoolean(false) }
        val disposed = remember { AtomicBoolean(false) }
        val cameraRef = remember { AtomicReference<androidx.camera.core.Camera?>(null) }
        val previewView = remember { PreviewView(context) }
        val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
        SideEffect { cameraRef.get()?.cameraControl?.enableTorch(torchOn) }
        DisposableEffect(lifecycleOwner, previewView) {
            val future = ProcessCameraProvider.getInstance(context)
            val mainExecutor = ContextCompat.getMainExecutor(context)
            val scanner = BarcodeScanning.getClient(
                com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build(),
            )
            future.addListener({
                val provider = future.get()
                if (disposed.get()) {
                    provider.unbindAll()
                    return@addListener
                }
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(analysisExecutor) { image ->
                    val media = image.image
                    if (media == null || handed.get()) {
                        image.close()
                        return@setAnalyzer
                    }
                    val input = InputImage.fromMediaImage(media, image.imageInfo.rotationDegrees)
                    scanner.process(input)
                        .addOnSuccessListener(mainExecutor) { codes ->
                            val text = codes.firstOrNull { it.rawValue != null }?.rawValue ?: return@addOnSuccessListener
                            if (!looksLikeAttendanceCode(text)) return@addOnSuccessListener
                            if (handed.compareAndSet(false, true)) onTextState.value(text)
                        }
                        .addOnCompleteListener { image.close() }
                }
                provider.unbindAll()
                if (disposed.get()) return@addListener
                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
                cameraRef.set(camera)
                camera.cameraControl.enableTorch(torchOn)
            }, mainExecutor)
            onDispose {
                disposed.set(true)
                cameraRef.set(null)
                if (future.isDone) future.get().unbindAll()
                analysisExecutor.shutdown()
                scanner.close()
            }
        }
        AndroidView(factory = { previewView }, modifier = modifier)
    }
}
