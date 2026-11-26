package ph.project.active.ui.platform

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * F7-AC31: the Android camera permission state maps to CameraAccess from three plain values (granted, the
 * rationale flag and the app's own "asked before" flag), so no device is needed. cameraAccessOf lives in
 * composeApp androidMain, package ph.project.active.ui.platform.
 */
class F7Ac31CameraAccessMappingTest {

    // F7-AC31: "| yes | any | any | GRANTED |"
    @Test
    fun F7_AC31_grantedIsGrantedWhateverTheFlags() {
        for (askedBefore in listOf(false, true)) {
            for (showRationale in listOf(false, true)) {
                assertEquals(
                    CameraAccess.GRANTED,
                    cameraAccessOf(granted = true, showRationale = showRationale, askedBefore = askedBefore),
                    "granted, askedBefore = $askedBefore, showRationale = $showRationale",
                )
            }
        }
    }

    // F7-AC31: "| no | no | — | NOT_ASKED |"
    @Test
    fun F7_AC31_neverAskedIsNotAsked() {
        for (showRationale in listOf(false, true)) {
            assertEquals(
                CameraAccess.NOT_ASKED,
                cameraAccessOf(granted = false, showRationale = showRationale, askedBefore = false),
                "not asked before, showRationale = $showRationale",
            )
        }
    }

    // F7-AC31: "| no | yes | yes | DENIED_CAN_ASK |"
    @Test
    fun F7_AC31_askedWithARationaleCanAskAgain() {
        assertEquals(CameraAccess.DENIED_CAN_ASK, cameraAccessOf(granted = false, showRationale = true, askedBefore = true))
    }

    // F7-AC31: "| no | yes | no | DENIED_FOR_GOOD |"
    @Test
    fun F7_AC31_askedWithoutARationaleIsDeniedForGood() {
        assertEquals(CameraAccess.DENIED_FOR_GOOD, cameraAccessOf(granted = false, showRationale = false, askedBefore = true))
    }
}
