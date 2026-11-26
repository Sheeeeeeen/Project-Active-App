package ph.project.active.ui.platform

/** Maps the three Android permission flags onto [CameraAccess]. */
fun cameraAccessOf(granted: Boolean, showRationale: Boolean, askedBefore: Boolean): CameraAccess = when {
    granted -> CameraAccess.GRANTED
    !askedBefore -> CameraAccess.NOT_ASKED
    showRationale -> CameraAccess.DENIED_CAN_ASK
    else -> CameraAccess.DENIED_FOR_GOOD
}
