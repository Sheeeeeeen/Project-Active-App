@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package ph.project.active.data.secure

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy
import ph.project.active.data.repository.SecretKeyStore

/**
 * Keeps each coach's private key across restarts as a Keychain generic-password item
 * (service [SIGNING_KEY_SERVICE], account = coach id, this device only, after first unlock).
 * A key that cannot be read is treated as missing, and the QR tab makes a new one.
 */
class IosSecretKeyStore : SecretKeyStore {
    override fun putPrivateKey(coachId: String, key: ByteArray) {
        val account = CFBridgingRetain(coachId)
        val service = CFBridgingRetain(SIGNING_KEY_SERVICE)
        val data = CFBridgingRetain(key.copyOf().toNsData())
        deleteItem(service, account)
        val query = dictionary(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to account,
            kSecValueData to data,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        )
        if (query != null) {
            SecItemAdd(query, null)
            CFRelease(query)
        }
        CFBridgingRelease(account)
        CFBridgingRelease(service)
        CFBridgingRelease(data)
    }

    override fun privateKey(coachId: String): ByteArray? {
        val account = CFBridgingRetain(coachId)
        val service = CFBridgingRetain(SIGNING_KEY_SERVICE)
        val query = dictionary(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to account,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne,
        )
        val key = if (query == null) null else readItem(query).also { CFRelease(query) }
        CFBridgingRelease(account)
        CFBridgingRelease(service)
        return key
    }

    private fun deleteItem(service: CFTypeRef?, account: CFTypeRef?) {
        val query = dictionary(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to account,
        ) ?: return
        SecItemDelete(query)
        CFRelease(query)
    }
}

private const val SIGNING_KEY_SERVICE = "ph.project.active.signing-key"

private fun readItem(query: CFDictionaryRef): ByteArray? = memScoped {
    val result = alloc<CFTypeRefVar>()
    if (SecItemCopyMatching(query, result.ptr) != errSecSuccess) return@memScoped null
    val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
    data.toByteArray()
}

private fun dictionary(vararg pairs: Pair<CFStringRef?, CFTypeRef?>): CFDictionaryRef? {
    val dict = CFDictionaryCreateMutable(null, pairs.size.convert(), null, null) ?: return null
    pairs.forEach { (key, value) -> CFDictionaryAddValue(dict, key, value) }
    return dict
}

private fun ByteArray.toNsData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNsData), length = size.convert())
}

private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    if (isEmpty()) return@apply
    usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
}
