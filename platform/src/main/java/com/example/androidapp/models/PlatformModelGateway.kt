package com.example.androidapp.models

import android.content.Context
import com.example.gameapi.GameModelGateway
import com.example.gameapi.GameModelProvider
import com.example.gameapi.GameModelSession
import com.example.gameapi.GameAssetReference
import com.example.gameapi.GameAssetGateway
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.ModelNode

class PlatformModelGateway(
    private val context: Context,
    providers: List<GameModelProvider>
) : GameModelGateway {
    private val providersByModelId: Map<String, GameModelProvider> = buildMap {
        providers.forEach { provider ->
            provider.modelIds.forEach { modelId ->
                check(put(modelId, provider) == null) {
                    "More than one provider registered for model '$modelId'."
                }
            }
        }
    }

    override val availableModelIds: Set<String>
        get() = providersByModelId.keys

    override fun openSession(modelId: String): GameModelSession {
        val provider = providersByModelId[modelId]
            ?: throw IllegalArgumentException("No model provider is registered for '$modelId'.")
        return provider.createSession(context, modelId)
    }
}

class PlatformAssetGateway(
    private val context: Context,
    assets: List<GameAssetReference>
) : GameAssetGateway {
    private val modelAssets = assets.associateBy(GameAssetReference::id).also { indexed ->
        require(indexed.size == assets.size) {
            "Game modules registered duplicate 3D asset IDs."
        }
    }

    override val availableAssetIds: Set<String> = modelAssets.keys

    override fun resolveModel(assetId: String): GameAssetReference {
        val asset = modelAssets[assetId]
            ?: throw IllegalArgumentException("No 3D asset is registered for '$assetId'.")
        val parent = asset.assetLocation.substringBeforeLast('/', "")
        val fileName = asset.assetLocation.substringAfterLast('/')
        val availableAssets = context.assets.list(parent).orEmpty()
        check(fileName in availableAssets) {
            "3D asset '$assetId' points to missing packaged file '${asset.assetLocation}'."
        }
        return asset
    }

    override suspend fun loadModelNode(
        assetId: String,
        modelLoader: ModelLoader
    ): ModelNode {
        val asset = resolveModel(assetId)
        val instance = modelLoader.loadModelInstance(asset.assetLocation)
            ?: throw IllegalStateException(
                "SceneView could not load 3D asset '${asset.id}' from '${asset.assetLocation}'."
            )
        return ModelNode(instance)
    }
}
