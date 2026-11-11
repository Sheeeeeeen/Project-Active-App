package ph.project.active.domain.crypto

/**
 * Platform Ed25519. The Kotlin stdlib has none, and the KMP library that ships Ed25519
 * (cryptography-kotlin 0.6.0) needs Kotlin 2.3, so Phase 1 uses an expect/actual split:
 * the JDK's built-in Ed25519 (JDK 15+) on JVM.
 */
internal expect fun ed25519Generate(): Ed25519KeyPair

internal expect fun ed25519Sign(privateKey: ByteArray, message: ByteArray): ByteArray

/** Returns false (never throws) for malformed keys or signatures. */
internal expect fun ed25519Verify(publicKey: ByteArray, message: ByteArray, signature: ByteArray): Boolean
