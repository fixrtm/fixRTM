package com.anatawa12.fixRtm.scripting.sai

import com.anatawa12.fixRtm.caching.ModelPackBasedCache
import com.anatawa12.fixRtm.fixCacheDir
import com.anatawa12.fixRtm.io.FIXModelPack
import com.anatawa12.fixRtm.io.FIXResource
import com.anatawa12.fixRtm.scripting.ExecutedScript
import com.anatawa12.fixRtm.scripting.IScriptRuntime
import com.anatawa12.fixRtm.utils.DigestUtils
import com.anatawa12.sai.ImporterTopLevel
import com.anatawa12.sai.Script
import com.anatawa12.sai.ScriptableObject
import com.anatawa12.sai.TopLevel
import net.minecraft.util.ResourceLocation

object SaiScriptRuntimeImpl : IScriptRuntime<Script, FIXSaiScriptEngine> {
    override fun getCachedEngine(
        filePath: ResourceLocation,
        resource: FIXResource,
        dependencies: Map<ResourceLocation, String>,
    ): FIXSaiScriptEngine? {
        return getScriptAndDoScriptByCache(filePath, resource.pack, makeDependenciesData(dependencies))
            ?.let { scope -> FIXSaiScriptEngine().also { it.scope = scope } }
    }

    override fun compile(script: String, fileName: String, engine: FIXSaiScriptEngine?): Script = usingContext {
        return ScriptCompiledClassCache.compile(script, fileName)
    }

    override fun exec(script: Script): FIXSaiScriptEngine {
        val engine = FIXSaiScriptEngine()
        usingContext { cx ->
            val scope = makeNewScope()

            script.exec(cx, scope)

            engine.scope = scope
        }

        return engine
    }

    override fun cache(
        pack: FIXModelPack,
        filePath: ResourceLocation,
        dependencies: Map<ResourceLocation, String>,
        engine: FIXSaiScriptEngine,
    ) {
        ExecutedScriptCache.add(pack, filePath, makeExecutedScript(makeDependenciesData(dependencies), engine.scope))
    }

    val baseScope = usingContext {
        val scope = TopLevel()

        it.initStandardObjects(scope)

        ImportScriptSaiFunctionImpl.init(scope)

        scope.sealObject()

        scope
    }

    ////////////////////////////////////////////////////////////////

    private fun makeNewScope(): ScriptableObject = usingContext {
        ScriptCompiledClassCache.initContext(it)

        val scope = ImporterTopLevel(it, false)
        scope.prototype = baseScope
        return scope
    }

    private fun makeNewScopeWithCache(cache: ExecutedScript): ScriptableObject? = usingContext {
        ScriptCompiledClassCache.initContext(it)

        return cache.getScope(baseScope)
    }

    private fun makeExecutedScript(dependencies: Map<String, ByteArray>, scope: ScriptableObject): ExecutedScript =
        usingContext {
            ScriptCompiledClassCache.initContext(it)

            return ExecutedScript(dependencies, scope, baseScope)
        }

    private fun getScriptAndDoScriptByCache(
        filePath: ResourceLocation,
        pack: FIXModelPack,
        dependencies: Map<String, ByteArray>,
    ): ScriptableObject? {
        val cache = ExecutedScriptCache.getScript(pack, filePath) ?: return null

        // verify cache
        if (cache.dependencies.keys != dependencies.keys) return null

        for ((name, hash) in dependencies) {
            if (!cache.dependencies[name]!!.contentEquals(hash)) return null
        }

        // load cache

        val newScope = makeNewScopeWithCache(cache)
        if (newScope == null) {
            ExecutedScriptCache.discord(pack, filePath)
        }
        return newScope
    }

    private fun makeDependenciesData(dependencies: Map<ResourceLocation, String>): Map<String, ByteArray> {
        val data = mutableMapOf<String, ByteArray>()
        for ((name, script) in dependencies) {
            data[name.toString()] = DigestUtils.sha1(script)
        }
        return data
    }
}

object ExecutedScriptCache {
    private val cache = ModelPackBasedCache(
        fixCacheDir.resolve("excluded-script"),
        0x0000 to ExecutedScript.Serializer
    )

    fun getScript(pack: FIXModelPack, filePath: ResourceLocation): ExecutedScript? {
        return cache.get(pack, DigestUtils.sha1Hex(filePath.toString()), ExecutedScript.Serializer)
    }

    fun add(pack: FIXModelPack, filePath: ResourceLocation, executedScript: ExecutedScript) {
        cache.put(pack, DigestUtils.sha1Hex(filePath.toString()), executedScript)
    }

    fun discord(pack: FIXModelPack, filePath: ResourceLocation) {
        cache.discord(pack, DigestUtils.sha1Hex(filePath.toString()))
    }

    fun load() {}
}


