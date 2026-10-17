plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val generatedStudioSources = layout.buildDirectory.dir("generated/studio-sources")
val copyStudioSources = tasks.register<Copy>("copyStudioSources") {
    val root = rootProject.projectDir
    from(root.resolve("app/src/main/java/com/example/androidapp/MainActivity.kt")) {
        into("platform")
    }
    from(root.resolve("game-api/src/main/java")) {
        include("**/*.kt")
        into("game-api")
    }
    from(root.resolve("platform/src/main/java/com/example/androidapp")) {
        include(
            "core/GameLauncherManager.kt",
            "loader/DynamicGameLoader.kt",
            "ui/screens/ArScreen.kt",
            "ui/screens/NativeAppScreen.kt",
            "ui/screens/SpatialMeasureScreen.kt",
            "ui/screens/StudioScreen.kt",
            "viewmodel/ArViewModel.kt",
            "models/PlatformModelGateway.kt",
            "models/MediaPipeHandModelProvider.kt"
        )
        into("platform")
    }
    from(root.resolve("games/fruit-ninja/src/main/java/com/example/androidapp")) {
        include(
            "games/FruitNinjaPlugin.kt",
            "ui/screens/ARFruitNinjaGame.kt",
            "mode1/FruitNinjaEngine.kt"
        )
        into("games/fruit-ninja")
    }
    from(root.resolve("games/spatial-runner/src/main/java/com/example/androidapp")) {
        include("games/SpatialRunnerGame.kt", "games/SpatialRunnerPlugin.kt")
        into("games/spatial-runner")
    }
    from(root.resolve("games/target-shooter/src/main/java/com/example/androidapp")) {
        include("games/TargetShooterGame.kt", "games/TargetShooterPlugin.kt")
        into("games/target-shooter")
    }
    from(root.resolve("games/room-editor/src/main/java/com/example/androidapp")) {
        include("games/ArStudioGame.kt", "mode2/RoomEditorEngine.kt")
        into("games/room-editor")
    }
    into(generatedStudioSources.map { it.dir("studio_sources") })
}

android {
    namespace = "com.example.androidapp.platform"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    sourceSets.getByName("main").assets.srcDir(generatedStudioSources)
}

tasks.named("preBuild").configure {
    dependsOn(copyStudioSources)
}

dependencies {
    api(project(":game-api"))
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("com.google.ar:core:1.41.0")
    implementation("io.github.sceneview:arsceneview:2.3.0")
    implementation("com.google.mediapipe:tasks-vision:latest.release")
}
