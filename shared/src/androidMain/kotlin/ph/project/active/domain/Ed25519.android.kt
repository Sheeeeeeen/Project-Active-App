package ph.project.active.domain

import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

// Same JDK Ed25519 actual as jvmMain. androidMain does not see jvmMain, and the shared
// module's Android target still has to compile the Phase 1 expect declarations.
// :shared:jvmTest runs the jvmMain actual. iOS stays the Phase 1 stub.
private const val ALGORITHM = "Ed25519"

internal actual fun ed25519Generate(): Ed25519KeyPair {
    val pair = KeyPairGenerator.getInstance(ALGORITHM).generateKeyPair()
    return Ed25519KeyPair(publicKey = pair.public.encoded, privateKey = pair.private.encoded)
}

internal actual fun ed25519Sign(privateKey: ByteArray, message: ByteArray): ByteArray {
    val key = KeyFactory.getInstance(ALGORITHM).generatePrivate(PKCS8EncodedKeySpec(privateKey))
    return Signature.getInstance(ALGORITHM).run {
        initSign(key)
        update(message)
        sign()
    }
}

internal actual fun ed25519Verify(publicKey: ByteArray, message: ByteArray, signature: ByteArray): Boolean = try {
    val key = KeyFactory.getInstance(ALGORITHM).generatePublic(X509EncodedKeySpec(publicKey))
    Signature.getInstance(ALGORITHM).run {
        initVerify(key)
        update(message)
        verify(signature)
    }
} catch (e: GeneralSecurityException) {
    false
} catch (e: IllegalArgumentException) {
    false
}
