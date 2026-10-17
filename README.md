# ArGame Platform

ArGame Platform is a native Android host for games built as separate Gradle modules. The shared `:game-api` module defines the contracts, `:platform` hosts the Android UI and platform services, and each game lives in its own `:games:*` library module.

## Module layout

- `:game-api` — game lifecycle, metadata, AI model, and 3D asset interfaces.
- `:platform` — Android host, launcher, model-provider routing, and generic 3D asset loading.
- `:games:fruit-ninja`, `:games:spatial-runner`, `:games:target-shooter`, `:games:room-editor` — independently maintained game modules.
- `:app` — Android application that composes the platform and registers the included games.

Games use `GameRuntime.models` to open model sessions; they do not call MediaPipe or other model implementations directly. Each game declares its own `GameAssetReference` entries through `GamePlugin.modelAssets`. The platform merges those declarations and makes the assets available through `GameRuntime.assets`.

## Adding a game

1. Create an Android library module under `games/` and add it to `settings.gradle.kts`.
2. Depend on `:game-api` and implement `GamePlugin` and `IMiniGame` in the module. Declare any module-owned 3D assets in `modelAssets`.
3. Add the module as an `:app` dependency and its plugin to the `games` list in `MainActivity`.
4. Build and install the host app.

The module can be developed and built separately, but this setup is compile-time modular integration: adding a game to an installed host requires rebuilding the app. Selecting an APK or ZIP at runtime does not install or execute game code.

## Build

On Windows, run `.\gradlew.bat :app:assembleDebug` from the repository root.
