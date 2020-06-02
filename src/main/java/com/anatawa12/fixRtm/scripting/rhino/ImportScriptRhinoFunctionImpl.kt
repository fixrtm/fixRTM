package com.anatawa12.fixRtm.scripting.rhino

import com.anatawa12.fixRtm.io.FIXFileLoader
import com.anatawa12.fixRtm.io.FIXModelPack
import com.anatawa12.fixRtm.scripting.ScriptImporter
import net.minecraft.util.ResourceLocation
import org.mozilla.javascript.*


object ImportScriptRhinoFunctionImpl : BaseFunction() {
    fun getScript(name: String): Script {
        val resourceLocation = ResourceLocation(name)
        val resource = FIXFileLoader.getResource(resourceLocation)
        val script = resource.inputStream.reader().use { it.readText() }
        return makeScript(resourceLocation, script, resource.pack)
    }

    fun makeScript(location: ResourceLocation, script: String, pack: FIXModelPack? = null): Script {
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
        usingContext { cx ->
            scope.defineProperty(
                    ImportScriptRhinoFunctionImpl.functionName,
                    ImportScriptRhinoFunctionImpl,
                    ScriptableObject.READONLY or ScriptableObject.DONTENUM
            )
        }
    }
}
