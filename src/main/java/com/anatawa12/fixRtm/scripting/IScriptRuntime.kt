/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.scripting

import com.anatawa12.fixRtm.io.FIXModelPack
import com.anatawa12.fixRtm.io.FIXResource
import net.minecraft.util.ResourceLocation
import javax.script.ScriptEngine

interface IScriptRuntime<CompiledScript, Engine : ScriptEngine> {
    /**
     * @param filePath the path of script
     * @param resource [FIXResource] of the script
     * @param dependencies all dependency script name and body
     * @return cached [Engine] if found, returns null if cache is not found or not supported.
     */
    fun getCachedEngine(
        filePath: ResourceLocation,
        resource: FIXResource,
        dependencies: Map<ResourceLocation, String>,
    ): Engine?

    /**
     * @param location the path of script
     * @param script body of script
     * @param pack model pack of the script
     * @param engine the engine for which runtime compile.
     */
    fun compile(
        location: ResourceLocation,
        script: String,
        pack: FIXModelPack? = null,
        engine: Engine? = null,
    ): CompiledScript {
        val preprocessed = ScriptImporter.preprocessScript(script)
        val name = if (pack != null) "$location(${pack.file.name})" else "$location"
        return compile(preprocessed, name, engine = engine)
    }

    /**
     * @param script body of script
     * @param fileName name of the script
     * @param engine the engine for which runtime compile.
     */
    fun compile(script: String, fileName: String, engine: Engine? = null): CompiledScript

    fun exec(script: CompiledScript): Engine

    fun cache(
        pack: FIXModelPack,
        filePath: ResourceLocation,
        dependencies: Map<ResourceLocation, String>,
        engine: Engine,
    )

    object AssertingRuntime : IScriptRuntime<Nothing, Nothing> {
        override fun getCachedEngine(
            filePath: ResourceLocation,
            resource: FIXResource,
            dependencies: Map<ResourceLocation, String>,
        ): Nothing? = throw AssertionError("IScriptRuntime should be never used")

        override fun compile(script: String, fileName: String, engine: Nothing?): Nothing =
            throw AssertionError("IScriptRuntime should be never used")

        override fun exec(script: Nothing): Nothing = throw AssertionError("IScriptRuntime should be never used")

        override fun cache(
            pack: FIXModelPack,
            filePath: ResourceLocation,
            dependencies: Map<ResourceLocation, String>,
            engine: Nothing,
        ) = throw AssertionError("IScriptRuntime should be never used")
    }
}
