package com.anatawa12.fixRtm.scripting

import com.anatawa12.fixRtm.io.FIXFileLoader
import net.minecraft.util.ResourceLocation
import org.mozilla.javascript.*
import java.util.concurrent.ConcurrentHashMap


object ScriptImporter {
    const val importScriptFunctionName = "__\$\$fixrtm_internal_function_importScript\$\$__"

    private val importScriptFunction = ImportScriptFunctionImpl as BaseFunction

    fun importScript(cx: Context, name: String, scope: Scriptable) {
        val script = getScript(name)
        script.exec(cx, scope)
    }

    fun getScript(name: String): Script {
        val resourceLocation = ResourceLocation(name)
        val script = FIXFileLoader.getInputStream(resourceLocation).reader().use { it.readText() }
        return makeScript(resourceLocation, script)
    }

    fun makeScript(name: ResourceLocation, script: String): Script {
        val script = preprocessScript(script)
        return ScriptCompiledClassCache.compile(script, name.toString())
    }

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


    private object ImportScriptFunctionImpl : BaseFunction() {
        override fun call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array<Any>): Any? {
            if (args.size != 1)
                ScriptRuntime.typeError("invalid arguments: argument length must be 1")
            val name = ScriptRuntime.toString(args[0])

            importScript(cx, name, scope)

            return Undefined.instance
        }

        override fun getFunctionName(): String = importScriptFunctionName
    }

    fun init(scope: ScriptableObject) {
        usingContext { cx ->
            scope.defineProperty(
                    importScriptFunction.functionName,
                    importScriptFunction,
                    ScriptableObject.READONLY or ScriptableObject.DONTENUM
            )
        }
    }
}
