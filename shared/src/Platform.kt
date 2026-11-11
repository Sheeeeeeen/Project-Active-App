package ph.com.sheen.projectactive

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
