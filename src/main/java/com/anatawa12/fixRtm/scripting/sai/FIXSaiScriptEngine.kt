/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.scripting.sai

import com.anatawa12.sai.Function
import com.anatawa12.sai.ScriptableObject
import java.io.Reader
import javax.script.*

class FIXSaiScriptEngine : ScriptEngine, Invocable {
    lateinit var scope: ScriptableObject

    override fun createBindings(): Bindings {
        TODO("Not yet implemented")
    }

    override fun setContext(context: ScriptContext?) {
        TODO("Not yet implemented")
    }

    override fun eval(script: String?, context: ScriptContext?): Any {
        TODO("Not yet implemented")
    }

    override fun eval(reader: Reader?, context: ScriptContext?): Any {
        TODO("Not yet implemented")
    }

    override fun eval(script: String?): Any {
        TODO("Not yet implemented")
    }

    override fun eval(reader: Reader?): Any {
        TODO("Not yet implemented")
    }

    override fun eval(script: String?, n: Bindings?): Any {
        TODO("Not yet implemented")
    }

    override fun eval(reader: Reader?, n: Bindings?): Any {
        TODO("Not yet implemented")
    }

    override fun getBindings(scope: Int): Bindings {
        TODO("Not yet implemented")
    }

    override fun put(key: String, value: Any?) {
        scope.put(key, scope, value)
    }

    override fun get(key: String): Any? {
        return scope[key]
    }

    override fun getFactory(): ScriptEngineFactory {
        TODO("Not yet implemented")
    }

    override fun setBindings(bindings: Bindings?, scope: Int) {
        TODO("Not yet implemented")
    }

    override fun getContext(): ScriptContext {
        TODO("Not yet implemented")
    }

    override fun invokeMethod(thiz: Any, name: String, vararg args: Any?): Any {
        TODO("Not yet implemented")
    }

    override fun invokeFunction(name: String?, vararg args: Any?): Any {
        val func = (scope[name] as? Function) ?: throw ScriptException("$name is not function")
        usingContext { ctx ->
            ctx.wrapFactory.isJavaPrimitiveWrap = false
            return func.call(ctx, func.parentScope, null, args
                .map { ctx.wrapFactory.wrap(ctx, scope, it, null) }
                .toTypedArray())
        }
    }

    override fun <T : Any?> getInterface(clasz: Class<T>?): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> getInterface(thiz: Any?, clasz: Class<T>?): T {
        TODO("Not yet implemented")
    }
}
