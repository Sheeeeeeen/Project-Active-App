package ph.project.active.data.secure

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import ph.project.active.data.repository.SecretKeyStore

/**
 * Keeps each coach's private key across restarts. An AES-GCM key in Android Keystore wraps the
 * bytes; the wrapped bytes live in app-private preferences, one entry per coach id. A key that
 * cannot be unwrapped is treated as missing, and the QR tab makes a new one.
 */
class AndroidSecretKeyStore(context: Context) : SecretKeyStore {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun putPrivateKey(coachId: String, key: ByteArray) {
        val wrapped = wrap(key.copyOf()) ?: return
        preferences.edit().putString(coachId, wrapped).commit()
    }

    override fun privateKey(coachId: String): ByteArray? {
        val stored = preferences.getString(coachId, null) ?: return null
        return unwrap(stored)
    }

    private fun wrap(key: ByteArray): String? = try {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, wrappingKey())
        val payload = cipher.iv + cipher.doFinal(key)
        Base64.encodeToString(payload, Base64.NO_WRAP)
    } catch (error: GeneralSecurityException) {
        null
    } catch (error: IllegalArgumentException) {
        null
    }

    private fun unwrap(stored: String): ByteArray? = try {
        val payload = Base64.decode(stored, Base64.NO_WRAP)
        if (payload.size <= GCM_IV_BYTES) return null
        val iv = payload.copyOfRange(0, GCM_IV_BYTES)
        val ciphertext = payload.copyOfRange(GCM_IV_BYTES, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, wrappingKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        cipher.doFinal(ciphertext)
    } catch (error: GeneralSecurityException) {
        null
    } catch (error: IllegalArgumentException) {
        null
    }

    private fun wrappingKey(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val existing = store.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }
}

private const val PREFS = "ph.project.active.signing-keys"
private const val KEY_ALIAS = "ph.project.active.signing-key"
private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_IV_BYTES = 12
private const val GCM_TAG_BITS = 128
