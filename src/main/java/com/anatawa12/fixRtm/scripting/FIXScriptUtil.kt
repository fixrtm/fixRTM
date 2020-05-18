@file:JvmName("FIXScriptUtil")

package com.anatawa12.fixRtm.scripting

import jp.ngt.rtm.modelpack.ModelPackManager
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.NativeObject
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

@Suppress("unused")
fun ModelPackManager.getScriptAndDoScript(fileName: String): ScriptEngine {
    val engine = FIXScriptEngine()
    usingContext { cx ->
        val scope = makeNewScope()

        val script = ScriptImporter.getScript(fileName)

        script.exec(cx, scope)

        engine.scope = scope
    }
    return engine
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
