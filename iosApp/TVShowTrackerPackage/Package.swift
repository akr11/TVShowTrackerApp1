// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "TVShowTrackerPackage",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(name: "TVShowTrackerPackage", targets: ["TVShowTrackerPackage"]),
    ],
    targets: [
        .binaryTarget(
            name: "Shared",
            path: "../../shared/build/XCFrameworks/release/Shared.xcframework"
        ),
        .target(
            name: "TVShowTrackerPackage",
            dependencies: ["Shared"],
            path: "Sources/TVShowTrackerPackage"
        ),
    ]
)
