package ph.project.active.presentation

import kotlin.test.Test

class F1Ac2SharedContractsTest {
    @Test
    fun F1_AC2_registrationAndSignInSetOpenSignedInRootMatchingCompletion() {
        Phase3SharedChecks.studentRegistrationFromClassList()
        Phase3SharedChecks.signInFromClassList()
        Phase3SharedChecks.navigationClearsAfterSuccess()
    }

    @Test
    fun F1_AC2_fakePasswordHasherVerifiesItsOwnHashOnly() {
        Phase3SharedChecks.fakePasswordHasherRoundTrip()
    }

    @Test
    fun F1_AC2_canSubmitFollowsVisibleFieldsAndSubmitting() {
        Phase3SharedChecks.canSubmitFollowsVisibleFields()
    }

    @Test
    fun F1_AC2_q3AndFlow1StringsMatchUiCopy() {
        Phase3SharedChecks.flow1CopyMatchesUiCopy()
    }
}
