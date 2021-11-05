/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

@file:JvmName("FIXScriptUtil")

package com.anatawa12.fixRtm.scripting

import com.anatawa12.fixRtm.asm.config.MainConfig
import com.anatawa12.fixRtm.io.FIXFileLoader
import com.anatawa12.fixRtm.scripting.nashorn.NashornScriptRuntimeImpl
import com.anatawa12.fixRtm.scripting.sai.SaiScriptRuntimeImpl
import jp.ngt.rtm.modelpack.ModelPackManager
import net.minecraft.util.ResourceLocation
import javax.script.ScriptEngine

val scriptRuntime: IScriptRuntime<*, *> = when (MainConfig.scriptingMode) {
    MainConfig.ScriptingMode.CacheWithSai -> SaiScriptRuntimeImpl
    MainConfig.ScriptingMode.BetterWithNashorn -> NashornScriptRuntimeImpl
    MainConfig.ScriptingMode.UseRtmNormal -> IScriptRuntime.AssertingRuntime
}

fun loadFIXScriptUtil() {}

@Suppress("unused")
fun ModelPackManager.getScriptAndDoScript(fileName: String): ScriptEngine =
    com.anatawa12.fixRtm.scripting.getScriptAndDoScript(fileName)

@Suppress("unused")
fun getScriptAndDoScript(fileName: String): ScriptEngine {
    return if (MainConfig.useOurScripting) {
        getScriptAndDoScript(scriptRuntime, fileName)
    } else {
        jp.ngt.ngtlib.io.ScriptUtil.doScript(ModelPackManager.INSTANCE.getScript(fileName))
    }
}

@Suppress("unused")
fun <CompiledScript, Engine : ScriptEngine> getScriptAndDoScript(
    runtime: IScriptRuntime<CompiledScript, Engine>,
    fileName: String,
): ScriptEngine {
    val filePath = ResourceLocation(fileName)
    val resource = FIXFileLoader.getResource(filePath)
    val scriptStr = resource.inputStream.reader().use { it.readText() }
    val dependencies = ScriptImporter.getAllDependenceScripts(filePath, scriptStr)

    runtime.getCachedEngine(filePath, resource, dependencies)?.let { return it }

    // then evalute
    val script = runtime.compile(filePath, scriptStr, resource.pack)

    val engine = runtime.exec(script)

    // add to cache

    runtime.cache(resource.pack, filePath, dependencies, engine)

    return engine
}
