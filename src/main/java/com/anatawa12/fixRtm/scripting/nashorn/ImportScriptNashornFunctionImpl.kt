/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.scripting.nashorn

import com.anatawa12.fixRtm.io.FIXFileLoader
import jdk.nashorn.api.scripting.AbstractJSObject
import jdk.nashorn.api.scripting.NashornScriptEngine
import jdk.nashorn.api.scripting.ScriptObjectMirror
import net.minecraft.util.ResourceLocation
import javax.script.CompiledScript
import javax.script.ScriptContext
import javax.script.SimpleScriptContext

//*
object ImportScriptNashornFunctionImpl : AbstractJSObject() {
    override fun isStrictFunction(): Boolean = false

    private val ctx = SimpleScriptContext()

    private fun getCompiled(engine: NashornScriptEngine?, name: String): CompiledScript {
        val resourceLocation = ResourceLocation(name)
        val resource = FIXFileLoader.getResource(resourceLocation)
        val script = resource.inputStream.reader().use { it.readText() }
        return NashornScriptRuntimeImpl.compile(resourceLocation, script, resource.pack,
            engine = engine?.let { FIXNashornScriptEngine(it, ctx) })
    }

    override fun call(thiz: Any?, vararg args: Any?): Any? {
        val str = args.getOrNull(0)

        if (str !is String) throw IllegalArgumentException("argument#1 must be string")
        if (thiz !is ScriptObjectMirror) throw IllegalArgumentException("this must be a scope")

        val engine = thiz.getMember("engine") as? NashornScriptEngine

        if (engine != null && engine !in CompiledImportedScriptCache.engines)
            throw IllegalStateException("current engine is not current thread engine")

        val compiled = getCompiled(engine, str.toString())

        val context = SimpleScriptContext()
        context.setBindings(thiz, ScriptContext.ENGINE_SCOPE)

        return compiled.eval(context)
    }

    override fun isFunction(): Boolean = true

    override fun getClassName(): String = "Function"

    override fun isArray(): Boolean = false
}
// */
