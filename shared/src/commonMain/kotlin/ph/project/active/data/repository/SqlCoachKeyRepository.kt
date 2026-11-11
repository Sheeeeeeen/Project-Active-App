package ph.project.active.data.repository

import ph.project.active.data.db.TrxDatabase
import ph.project.active.domain.repository.CoachKeyRepository
import ph.project.active.domain.crypto.Ed25519KeyPair

class SqlCoachKeyRepository(
    private val database: TrxDatabase,
    private val secrets: SecretKeyStore,
) : CoachKeyRepository {
    override fun save(coachId: String, keyPair: Ed25519KeyPair) {
        database.trxDatabaseQueries.upsertPublicKey(coachId, keyPair.publicKey)
        secrets.putPrivateKey(coachId, keyPair.privateKey)
    }

    override fun find(coachId: String): Ed25519KeyPair? {
        val publicKey = database.trxDatabaseQueries.selectPublicKey(coachId).executeAsOneOrNull() ?: return null
        val privateKey = secrets.privateKey(coachId) ?: ByteArray(0)
        return Ed25519KeyPair(publicKey = publicKey, privateKey = privateKey)
    }
}
