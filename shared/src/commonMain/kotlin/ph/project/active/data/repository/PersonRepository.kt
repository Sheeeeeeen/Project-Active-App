package ph.project.active.data.repository

import ph.project.active.data.model.PersonProfile

interface PersonRepository {
    fun find(id: String): PersonProfile?
    fun upsert(profile: PersonProfile)
}
