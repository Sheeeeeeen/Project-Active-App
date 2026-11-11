import SwiftUI
import UIKit
import ComposeApp

/// Thin host: all screens are Compose Multiplatform UI from the composeApp module.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
