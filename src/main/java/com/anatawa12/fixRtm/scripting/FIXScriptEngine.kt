package com.anatawa12.fixRtm.scripting

import org.mozilla.javascript.Function
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject
import java.io.Reader
import javax.script.*

class FIXScriptEngine : ScriptEngine, Invocable {
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

    override fun invokeMethod(thiz: Any , name: String, vararg args: Any?): Any {
        TODO("Not yet implemented")
    }

    override fun invokeFunction(name: String?, vararg args: Any?): Any {
        val func = (scope[name] as? Function) ?: throw ScriptException("$name is not function")
        usingContext { ctx ->
            return func.call(ctx, func.parentScope, null, args)
        }
    }

    override fun <T : Any?> getInterface(clasz: Class<T>?): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> getInterface(thiz: Any?, clasz: Class<T>?): T {
        TODO("Not yet implemented")
    }
}
