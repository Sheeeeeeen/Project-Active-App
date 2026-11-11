package ph.project.active.domain.model

import ph.project.active.domain.usecase.SignIn

/** Thrown by [SignIn] for an unknown email or a wrong password (deliberately the same error). */
class AuthFailed : Exception("Email or password is incorrect")
