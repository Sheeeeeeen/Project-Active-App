# iosApp (thin Xcode host)

This folder holds the Swift entry point that embeds the Compose Multiplatform UI from
`composeApp` (framework `ComposeApp`, entry `MainViewControllerKt.MainViewController()`).
There is no SwiftUI UI here; screens are written once in `composeApp`.

The Xcode project file (`iosApp.xcodeproj`) is not generated on the Linux box. On a Mac, create it
from the official KMP wizard's iosApp template (or a new iOS App target) and add a Run Script build
phase before "Compile Sources":

```
cd "$SRCROOT/.."
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

Then add these two Swift files to the target. iOS is not built in Phase 1.
