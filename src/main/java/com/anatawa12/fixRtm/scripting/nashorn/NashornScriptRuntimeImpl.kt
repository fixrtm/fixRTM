/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

@file:JvmName("FIXScriptUtil")

package com.anatawa12.fixRtm.scripting.nashorn

import com.anatawa12.fixRtm.io.FIXModelPack
import com.anatawa12.fixRtm.io.FIXResource
import com.anatawa12.fixRtm.scripting.IScriptRuntime
import com.anatawa12.fixRtm.scripting.ScriptImporter
import net.minecraft.util.ResourceLocation
import javax.script.CompiledScript
import javax.script.ScriptContext
import javax.script.SimpleScriptContext

object NashornScriptRuntimeImpl : IScriptRuntime<CompiledScript, FIXNashornScriptEngine> {
    /**
     * nashorn does not support executed cache so always return null.
     * @return always null
     */
    override fun getCachedEngine(
        filePath: ResourceLocation,
        resource: FIXResource,
        dependencies: Map<ResourceLocation, String>,
    ): FIXNashornScriptEngine? {
        return null
    }

    override fun compile(script: String, fileName: String, engine: FIXNashornScriptEngine?): CompiledScript {
        return CompiledImportedScriptCache.makeCompiled(engine?.baseEngine, script, fileName)
    }

    override fun exec(script: CompiledScript): FIXNashornScriptEngine {
        val engine = CompiledImportedScriptCache.engine

        val context = SimpleScriptContext()
        val bindings = engine.createBindings()
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE)

        engine.eval("""load("nashorn:mozilla_compat.js");""", context)

        bindings[ScriptImporter.importScriptFunctionName] = ImportScriptNashornFunctionImpl

        script.eval(context)

        return FIXNashornScriptEngine(engine, context)
    }

    override fun cache(
        pack: FIXModelPack,
        filePath: ResourceLocation,
        dependencies: Map<ResourceLocation, String>,
        engine: FIXNashornScriptEngine,
    ) {
        // not supported so nop
    }
}
