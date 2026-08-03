package com.example.androidapp.loader

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/**
 * DYNAMIC MINI-GAME PACKAGE MODULE SPECIFICATION
 * 
 * Defines how external game packages (.apk, .zip, .bundle, or .glb) selected
 * from phone local storage via Intent.ACTION_GET_CONTENT are parsed, validated,
 * and dynamically instantiated via IMiniGame plugin contract.
 */
data class DynamicGamePackage(
    val packageId: String,
    val title: String,
    val developer: String,
    val version: String,
    val iconUri: String? = null,
    val entryClass: String = "com.example.minigame.MainGameModule",
    val modelAssetPath: String? = null,
    val isLoaded: Boolean = false,
    val fileSizeKb: Long = 0
)

interface IMiniGameModule {
    fun onInitialize(context: Context, sceneAnchor: Any)
    fun onFrameUpdate(deltaSeconds: Float, indexFingerRayX: Float, indexFingerRayY: Float, isPinching: Boolean)
    fun onGameEvent(eventName: String, payload: Map<String, Any>)
    fun onDestroy()
}

/**
 * Storage Access Framework (SAF) File Importer & Dynamic Package Registry
 */
class DynamicGameLoader(private val context: Context) {
    private val installedModules = mutableListOf<DynamicGamePackage>()

    /**
     * Imports a user-selected game package (.zip, .bundle, or .apk) from phone storage.
     * Uses Storage Access Framework ContentResolver to copy and extract package contents.
     */
    fun importPackageFromUri(uri: Uri): DynamicGamePackage? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val destDir = File(context.filesDir, "dynamic_games/${System.currentTimeMillis()}")
            destDir.mkdirs()

            val buffer = ByteArray(8192)
            val zipStream = ZipInputStream(inputStream)
            var entry = zipStream.nextEntry

            var gameTitle = "Custom Game Package"
            var modelPath: String? = null

            while (entry != null) {
                val file = File(destDir, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    val fos = FileOutputStream(file)
                    var bytesRead: Int
                    while (zipStream.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                    }
                    fos.close()
                }

                if (entry.name.endsWith("manifest.json")) {
                    gameTitle = "Loaded Custom Game"
                } else if (entry.name.endsWith(".glb") || entry.name.endsWith(".gltf")) {
                    modelPath = file.absolutePath
                }

                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
            zipStream.close()

            val pkg = DynamicGamePackage(
                packageId = "pkg_${System.currentTimeMillis()}",
                title = gameTitle,
                developer = "User Storage Import",
                version = "1.0.0",
                modelAssetPath = modelPath,
                isLoaded = true,
                fileSizeKb = destDir.length() / 1024
            )
            installedModules.add(pkg)
            pkg
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getInstalledModules(): List<DynamicGamePackage> = installedModules
}
