import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}