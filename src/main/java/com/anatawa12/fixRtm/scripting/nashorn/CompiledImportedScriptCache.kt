package com.anatawa12.fixRtm.scripting.nashorn

import com.anatawa12.fixRtm.utils.DigestUtils
import jdk.nashorn.api.scripting.NashornScriptEngine
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import javax.script.CompiledScript
import javax.script.ScriptContext

//*
object CompiledImportedScriptCache {
    private val local = ThreadLocal.withInitial { Threaded() }
    private val threaded get() = local.get()!!
    private val threadedByEngine = ConcurrentHashMap<NashornScriptEngine, Threaded>()

    private fun threadedByEngine(engine: NashornScriptEngine?): Threaded {
        if (engine == null) return threaded
        else return threadedByEngine[engine] ?: throw IllegalArgumentException("the engine is not threaded")
    }

    val engineFactory = NashornScriptEngineFactory()
    val engines = CopyOnWriteArraySet<NashornScriptEngine>()

    val engine get() = threaded.engine

    fun makeCompiled(script: String, fileName: String): CompiledScript = threaded.makeCompiled(script, fileName)

    fun makeCompiled(engine: NashornScriptEngine?, script: String, fileName: String): CompiledScript =
        threadedByEngine(engine).makeCompiled(script, fileName)

    fun load() {
        // load
    }

    private class Threaded {
        val engine = engineFactory.scriptEngine as NashornScriptEngine

        init {
            engines.add(engine)
            threadedByEngine[engine] = this
        }

        private val compiledScripts = mutableMapOf<String, CompiledScript>()

        fun makeCompiled(script: String, fileName: String): CompiledScript {
            engine.context.setAttribute("javax.script.filename", fileName, ScriptContext.ENGINE_SCOPE)
            val compile = engine.compile(script)
            compiledScripts[DigestUtils.sha1Hex(script)] = compile
            engine.context.removeAttribute("javax.script.filename", ScriptContext.ENGINE_SCOPE)
            return compile
        }

    }
}
// */
