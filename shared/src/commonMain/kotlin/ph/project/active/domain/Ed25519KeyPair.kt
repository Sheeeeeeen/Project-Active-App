package ph.project.active.domain

/**
 * A coach's Ed25519 signing key pair. Keys are opaque platform encodings
 * (X.509 SubjectPublicKeyInfo for the public key, PKCS#8 for the private key).
 * The private key never appears in [toString].
 */
class Ed25519KeyPair(val publicKey: ByteArray, internal val privateKey: ByteArray) {

    fun sign(message: ByteArray): ByteArray = ed25519Sign(privateKey, message)

    fun verify(message: ByteArray, signature: ByteArray): Boolean =
        ed25519Verify(publicKey, message, signature)

    override fun toString(): String = "Ed25519KeyPair(publicKey=${publicKey.size} bytes, privateKey=***)"

    companion object {
        fun generate(): Ed25519KeyPair = ed25519Generate()
    }
}
