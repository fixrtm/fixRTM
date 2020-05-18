@file:JvmName("FIXScriptUtil")

package com.anatawa12.fixRtm.scripting

import com.anatawa12.fixRtm.caching.ModelPackBasedCache
import com.anatawa12.fixRtm.caching.TaggedFileManager
import com.anatawa12.fixRtm.fixCacheDir
import com.anatawa12.fixRtm.io.FIXFileLoader
import com.anatawa12.fixRtm.io.FIXModelPack
import com.anatawa12.fixRtm.utils.DigestUtils
import jp.ngt.rtm.modelpack.ModelPackManager
import kotlinx.coroutines.withContext
import net.minecraft.util.ResourceLocation
import org.mozilla.javascript.Context
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.serialize.ScriptableInputStream
import org.mozilla.javascript.serialize.ScriptableOutputStream
import java.io.*
import javax.script.ScriptEngine

val baseScope = usingContext {
    val scope = ImporterTopLevel(it, false)

    ScriptImporter.init(scope)

    scope
}

private fun makeNewScope(): NativeObject {
    val scope = NativeObject()
    scope.prototype = baseScope
    return scope
}

fun makeNewScopeWithCache(cache: ExecutedScript): NativeObject {
    usingContext {
        ScriptCompiledClassCache.initContext(it)
        return cache.getScope(baseScope)
    }
}

fun makeExecutedScript(dependencies: Map<String, ByteArray>, scope: NativeObject): ExecutedScript {
    usingContext {
        ScriptCompiledClassCache.initContext(it)
        return ExecutedScript(dependencies, scope, baseScope)
    }
}

@Suppress("unused")
fun ModelPackManager.getScriptAndDoScript(fileName: String): ScriptEngine {
    val filePath = ResourceLocation(fileName)
    val resource = FIXFileLoader.getResource(filePath)
    val script = resource.inputStream.reader().use { it.readText() }
    val dependencies = makeDependenciesData(ScriptImporter.getAllDependenceScripts(filePath, script))

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

        val script = ScriptImporter.getScript(fileName)

        script.exec(cx, scope)

        engine.scope = scope
    }

    // add to cache

    ExecutedScriptCache.add(resource.pack, filePath, makeExecutedScript(dependencies, engine.scope))

    return engine
}

fun getScriptAndDoScriptByCache(filePath: ResourceLocation, pack: FIXModelPack, dependencies: Map<String, ByteArray>): NativeObject? {
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
fun getScriptAndDoScript(fileName: String): ScriptEngine {
    val engine = FIXScriptEngine()
    usingContext { cx ->
        val scope = makeNewScope()

        val script = ScriptImporter.getScript(fileName)

        script.exec(cx, scope)

        engine.scope = scope
    }
    return engine
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
}

class ExecutedScript private constructor(
        /**
         * dependency name and sha1 hash of script.
         */
        val dependencies: Map<String, ByteArray>,
        val scopeData: ByteArray
) {
    constructor(
            dependencies: Map<String, ByteArray>,
            scope: NativeObject,
            base: Scriptable
    ): this(
            dependencies, writeScopeData(scope, base)
    ) {
        for ((name, hash) in dependencies) {
            require(hash.size == 20) { "dependencies[$name] is not valid sha1: size is not 20" }
        }
    }

    fun getScope(base: Scriptable) = readScopeData(scopeData, base)

    object Serializer : TaggedFileManager.Serializer<ExecutedScript> {
        override val type: Class<ExecutedScript> get() = ExecutedScript::class.java

        override fun serialize(stream: OutputStream, value: ExecutedScript) {
            val stream = DataOutputStream(stream)
            stream.writeInt(value.dependencies.size)
            for ((name, data) in value.dependencies) {
                stream.writeUTF(name)
                stream.write(data)
            }
            stream.write(value.scopeData)

        }

        override fun deserialize(stream: InputStream): ExecutedScript {
            val stream = DataInputStream(stream)

            val dependencies = mutableMapOf<String, ByteArray>()
            val count = stream.readInt()
            repeat(count) {
                val name = stream.readUTF()
                val hash = ByteArray(20)
                stream.readFully(hash)
                dependencies[name] = hash
            }

            val scopeData = stream.readBytes()

            return ExecutedScript(
                    dependencies,
                    scopeData
            )
        }
    }

    companion object {
        /**
         * user have to set [Context]
         */
        private fun writeScopeData(scope: NativeObject, base: Scriptable): ByteArray {
            val baos = ByteArrayOutputStream()
            ScriptableOutputStream(baos, base).use { stream ->
                stream.writeObject(scope)
            }
            return baos.toByteArray()
        }

        /**
         * user have to set [Context]
         */
        private fun readScopeData(data: ByteArray, base: Scriptable): NativeObject {
            val bais = ByteArrayInputStream(data)
            ScriptableInputStream(bais, base).use { stream ->
                return stream.readObject() as NativeObject
            }
        }
    }
}
