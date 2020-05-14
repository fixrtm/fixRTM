package com.anatawa12.fixRtm.caching

import com.anatawa12.fixRtm.fixCacheDir
import com.anatawa12.fixRtm.io.FIXFileLoader
import com.anatawa12.fixRtm.io.FIXModelPack
import com.anatawa12.fixRtm.ngtlib.renderer.model.CachedPolygonModel
import com.anatawa12.fixRtm.ngtlib.renderer.model.FileCache
import com.anatawa12.fixRtm.threadFactoryWithPrefix
import jp.ngt.ngtlib.renderer.model.PolygonModel
import java.util.concurrent.Executors

object ModelPackBasedCache {
    private val baseDir = fixCacheDir.resolve("modelpack-base")

    private val caches: Map<FIXModelPack, FileCache<Any>>
    private val taggedFileManager = TaggedFileManager()

    init {
        taggedFileManager.register(0x0000, CachedPolygonModel.Serializer)
    }

    init {
        val modelName = FIXFileLoader.allModelPacks.mapTo(mutableSetOf()) { it.file.name }

        for (removedNames in (baseDir.list()?.toSet().orEmpty() - modelName)) {
            baseDir.resolve(removedNames).deleteRecursively()
        }

        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                threadFactoryWithPrefix("jasm-model-cache-creating"))

        val caches = mutableMapOf<FIXModelPack, FileCache<Any>>()

        for (modelPack in FIXFileLoader.allModelPacks) {
            val cache = FileCache(
                    baseDir = baseDir.resolve(modelPack.file.name),
                    baseDigest = modelPack.sha1Hash,
                    executor = executor,
                    serialize = taggedFileManager::serialize,
                    deserialize = taggedFileManager::deserialize,
                    withTwoCharDir = false
            )
            cache.loadAll()
            caches[modelPack] = cache
        }

        this.caches = caches
    }

    fun <T : Any> get(pack: FIXModelPack, sha1: String, serializer: TaggedFileManager.Serializer<T>): T? {
        return serializer.type.cast(caches[pack]?.getCachedValue(sha1))
    }

    fun put(pack: FIXModelPack, sha1: String, model: PolygonModel) {
        caches[pack]?.putCachedValue(sha1, model)
    }

}
