package ph.project.active.data

interface PersonRepository {
    fun find(id: String): PersonProfile?
    fun upsert(profile: PersonProfile)
}
