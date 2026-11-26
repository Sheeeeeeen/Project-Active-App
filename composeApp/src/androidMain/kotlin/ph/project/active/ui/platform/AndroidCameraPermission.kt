package ph.project.active.ui.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberCameraPermission(): CameraPermission {
    val context = LocalContext.current
    val permission = remember(context.applicationContext) { AndroidCameraPermission(context.applicationContext) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permission.deliver(
            granted,
            context.findActivity()?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == true,
        )
    }
    SideEffect {
        permission.activity = context.findActivity()
        permission.launch = { launcher.launch(Manifest.permission.CAMERA) }
    }
    DisposableEffect(permission) {
        onDispose { permission.activity = null }
    }
    return permission
}

private class AndroidCameraPermission(private val context: Context) : CameraPermission {
    var launch: () -> Unit = {}
    var activity: Activity? = null
    private var pending: ((CameraAccess) -> Unit)? = null
    private val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun current(): CameraAccess = cameraAccessOf(
        granted = granted(),
        showRationale = activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == true,
        askedBefore = preferences.getBoolean(ASKED, false),
    )

    override fun request(onResult: (CameraAccess) -> Unit) {
        pending = onResult
        preferences.edit().putBoolean(ASKED, true).apply()
        launch()
    }

    override fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun deliver(granted: Boolean, showRationale: Boolean) {
        pending?.invoke(cameraAccessOf(granted = granted, showRationale = showRationale, askedBefore = true))
        pending = null
    }

    private fun granted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private const val PREFS = "ph.project.active.camera"
private const val ASKED = "asked"
