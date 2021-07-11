/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.scripting.sai

import com.anatawa12.fixRtm.io.FIXFileLoader
import com.anatawa12.fixRtm.io.FIXModelPack
import com.anatawa12.fixRtm.scripting.ScriptImporter
import com.anatawa12.sai.*
import net.minecraft.util.ResourceLocation


object ImportScriptSaiFunctionImpl : BaseFunction() {
    fun getScript(name: String): Script {
        val resourceLocation = ResourceLocation(name)
        val resource = FIXFileLoader.getResource(resourceLocation)
        val script = resource.inputStream.reader().use { it.readText() }
        return makeScript(resourceLocation, script, resource.pack)
    }

    fun makeScript(location: ResourceLocation, script: String, pack: FIXModelPack? = null): Script {
        @Suppress("NAME_SHADOWING")
        val script = ScriptImporter.preprocessScript(script)
        val name = if (pack != null) "$location(${pack.file.name})" else "$location"
        return ScriptCompiledClassCache.compile(script, name)
    }

    fun importScript(cx: Context, name: String, scope: Scriptable) {
        val script = getScript(name)
        script.exec(cx, scope)
    }

    override fun call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array<Any>): Any? {
        if (args.size != 1)
            ScriptRuntime.typeError("invalid arguments: argument length must be 1")
        val name = ScriptRuntime.toString(args[0])

        importScript(cx, name, scope)

        return Undefined.instance
    }

    override fun getFunctionName(): String = ScriptImporter.importScriptFunctionName

    fun init(scope: ScriptableObject) {
        usingContext {
            scope.defineProperty(
                ImportScriptSaiFunctionImpl.functionName,
                ImportScriptSaiFunctionImpl,
                ScriptableObject.READONLY or ScriptableObject.DONTENUM
            )
        }
    }
}
