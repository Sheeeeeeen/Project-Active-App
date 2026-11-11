package ph.project.active

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform