package ph.project.active.domain.model

/** Unknown email and wrong password throw this same error. */
class AuthFailed : Exception("Email or password is incorrect")
