package ph.project.active.presentation

import kotlin.test.Test

class P1OpenSignedInRootTest {
    @Test
    fun P1_studentRegistrationSetsOpenSignedInRootMatchingCompletion() {
        Phase3SharedChecks.studentRegistrationFromClassList()
    }

    @Test
    fun P1_coachRegistrationSetsOpenSignedInRootMatchingCompletion() {
        Phase3SharedChecks.coachRegistrationFromClassList()
    }

    @Test
    fun P1_requestToJoinRegistrationSetsOpenClassId() {
        Phase3SharedChecks.studentRegistrationFromRequestToJoin()
    }

    @Test
    fun P1_signInSetsOpenSignedInRootForTheAccountRole() {
        Phase3SharedChecks.signInFromClassList()
    }

    @Test
    fun P1_coachSignInFromRequestOpensCoachRootWithoutAClass() {
        Phase3SharedChecks.coachSignInFromRequestDoesNotOpenAClass()
    }

    @Test
    fun P1_onNavigationHandledClearsOpenSignedInRoot() {
        Phase3SharedChecks.navigationClearsAfterSuccess()
    }
}
