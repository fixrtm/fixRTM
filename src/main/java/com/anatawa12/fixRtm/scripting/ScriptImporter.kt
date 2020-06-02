package com.anatawa12.fixRtm.scripting

import com.anatawa12.fixRtm.io.FIXFileLoader
import net.minecraft.util.ResourceLocation
import org.mozilla.javascript.ScriptRuntime
import java.util.concurrent.ConcurrentHashMap


object ScriptImporter {
    const val importScriptFunctionName = "__\$\$fixrtm_internal_function_importScript\$\$__"

    private val allDependenceScripts = ConcurrentHashMap<ResourceLocation, Map<ResourceLocation, String>>()

    fun getAllDependenceScripts(name: String): Map<ResourceLocation, String> {
        val scriptPath = ResourceLocation(name)
        val script = FIXFileLoader.getInputStream(scriptPath).reader().use { it.readText() }
        return getAllDependenceScripts(scriptPath, script)
    }

    fun getAllDependenceScripts(scriptPath: ResourceLocation, script: String): Map<ResourceLocation, String> {
        allDependenceScripts[scriptPath]?.let { return it }
        val dependencies = mutableMapOf<ResourceLocation, String>()
        dependencies[scriptPath] = script
        for (matchResult in jsIncludePath.findAll(script)) {
            val includeScriptName = matchResult.groupValues[1]
            dependencies += getAllDependenceScripts(includeScriptName)
        }
        allDependenceScripts[scriptPath] = dependencies
        return dependencies
    }

    private val jsIncludePath = "//include <(.+)>".toRegex()

    fun preprocessScript(rawScript: String): String {
        return jsIncludePath.replace(rawScript) { result ->
            """;$importScriptFunctionName("${ScriptRuntime.escapeString(result.groupValues[1])}");"""
        }
    }
}
