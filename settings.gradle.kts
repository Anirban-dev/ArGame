pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ArGamePlatform"
include(
    ":app",
    ":platform",
    ":game-api",
    ":games:fruit-ninja",
    ":games:spatial-runner",
    ":games:target-shooter",
    ":games:room-editor"
)
