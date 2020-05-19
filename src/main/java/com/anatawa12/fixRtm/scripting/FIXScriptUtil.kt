@file:JvmName("FIXScriptUtil")

package com.anatawa12.fixRtm.scripting

import com.anatawa12.fixRtm.caching.ModelPackBasedCache
import com.anatawa12.fixRtm.fixCacheDir
import com.anatawa12.fixRtm.io.FIXFileLoader
import com.anatawa12.fixRtm.io.FIXModelPack
import com.anatawa12.fixRtm.utils.DigestUtils
import jp.ngt.rtm.modelpack.ModelPackManager
import net.minecraft.util.ResourceLocation
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.TopLevel
import javax.script.ScriptEngine

val baseScope = usingContext {
    val scope = TopLevel()

    it.initStandardObjects(scope)

    ScriptImporter.init(scope)

    scope
}

fun loadFIXScriptUtil() {}

private fun makeNewScope(): ScriptableObject = usingContext {
    val scope = ImporterTopLevel(it, false)
    scope.prototype = baseScope
    return scope
}

fun makeNewScopeWithCache(cache: ExecutedScript): ScriptableObject {
    usingContext {
        ScriptCompiledClassCache.initContext(it)
        return cache.getScope(baseScope)
    }
}

fun makeExecutedScript(dependencies: Map<String, ByteArray>, scope: ScriptableObject): ExecutedScript {
    usingContext {
        ScriptCompiledClassCache.initContext(it)
        return ExecutedScript(dependencies, scope, baseScope)
    }
}

@Suppress("unused")
fun ModelPackManager.getScriptAndDoScript(fileName: String): ScriptEngine {
    val filePath = ResourceLocation(fileName)
    val resource = FIXFileLoader.getResource(filePath)
    val scriptStr = resource.inputStream.reader().use { it.readText() }
    val dependencies = makeDependenciesData(ScriptImporter.getAllDependenceScripts(filePath, scriptStr))

    // first, try cache
    getScriptAndDoScriptByCache(filePath, resource.pack, dependencies)?.let { scope ->
        val engine = FIXScriptEngine()
        engine.scope = scope
        return engine
    }

    // then evalute
    val engine = FIXScriptEngine()
    usingContext { cx ->
        val scope = makeNewScope()

        val script = ScriptImporter.makeScript(filePath, scriptStr)

        script.exec(cx, scope)

        engine.scope = scope
    }

    // add to cache

    ExecutedScriptCache.add(resource.pack, filePath, makeExecutedScript(dependencies, engine.scope))

    return engine
}

fun getScriptAndDoScriptByCache(filePath: ResourceLocation, pack: FIXModelPack, dependencies: Map<String, ByteArray>): ScriptableObject? {
    val cache = ExecutedScriptCache.getScript(pack, filePath) ?: return null

    // verify cache
    if (cache.dependencies.keys != dependencies.keys) return null

    for ((name, hash) in dependencies) {
        if (!cache.dependencies[name]!!.contentEquals(hash)) return null
    }

    // load cache

    return makeNewScopeWithCache(cache)
}

fun makeDependenciesData(dependencies: Map<ResourceLocation, String>): Map<String, ByteArray> {
    val data = mutableMapOf<String, ByteArray>()
    for ((name, script) in dependencies) {
        data[name.toString()] = DigestUtils.sha1(script)
    }
    return data
}

@Suppress("unused")
fun getScriptAndDoScript(fileName: String): ScriptEngine = ModelPackManager.INSTANCE.getScriptAndDoScript(fileName)

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
}

