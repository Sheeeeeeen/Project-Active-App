package ph.project.active.domain

// Phase 1 builds and tests only the JVM target on Linux; iOS is not compiled here.
// TODO(phase 2): back these with CryptoKit Curve25519.Signing (via a Swift bridge) or a KMP
// library once the project's Kotlin version allows it (cryptography-kotlin 0.6.0 needs Kotlin 2.3).

internal actual fun ed25519Generate(): Ed25519KeyPair = ed25519Unsupported()

internal actual fun ed25519Sign(privateKey: ByteArray, message: ByteArray): ByteArray = ed25519Unsupported()

internal actual fun ed25519Verify(publicKey: ByteArray, message: ByteArray, signature: ByteArray): Boolean =
    ed25519Unsupported()

private fun ed25519Unsupported(): Nothing =
    throw UnsupportedOperationException("Ed25519 is not implemented on iOS yet (Phase 1 is JVM-only)")
