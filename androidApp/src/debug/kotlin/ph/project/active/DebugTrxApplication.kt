package ph.project.active

class DebugTrxApplication : TrxApplication() {
    override fun onCreate() {
        super.onCreate()
        DemoSeed.install()
    }
}
