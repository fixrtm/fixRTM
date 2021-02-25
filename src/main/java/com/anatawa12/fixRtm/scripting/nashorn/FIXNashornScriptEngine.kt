package com.anatawa12.fixRtm.scripting.nashorn

import jdk.nashorn.api.scripting.NashornScriptEngine
import java.io.Reader
import javax.script.*

class FIXNashornScriptEngine(
    val baseEngine: NashornScriptEngine,
    private val context: ScriptContext,
) : ScriptEngine by baseEngine, Compilable, Invocable {

    override fun getFactory(): ScriptEngineFactory {
        TODO("Not yet implemented")
    }

    override fun setContext(context: ScriptContext) {
        throw UnsupportedOperationException("setContext is not supported by FIXNashornScriptEngine")
    }

    override fun eval(script: String): Any? = baseEngine.eval(script, context)

    override fun eval(reader: Reader): Any? = baseEngine.eval(reader, context)

    override fun getBindings(scope: Int): Bindings = context.getBindings(scope)

    override fun put(name: String, value: Any?) {
        context.getBindings(ScriptContext.ENGINE_SCOPE)?.put(name, value)
    }

    override fun get(name: String): Any? = context.getBindings(ScriptContext.ENGINE_SCOPE)?.get(name)

    override fun setBindings(bindings: Bindings?, scope: Int) {
        context.setBindings(bindings, scope)
    }

    override fun getContext(): ScriptContext = context

    override fun compile(script: String): CompiledScript {
        val compiled = baseEngine.compile(script)
        return object : CompiledScript() {
            override fun eval(context: ScriptContext): Any = compiled.eval(context)

            override fun getEngine(): ScriptEngine = this@FIXNashornScriptEngine
        }
    }

    override fun compile(script: Reader): CompiledScript {
        val compiled = baseEngine.compile(script)
        return object : CompiledScript() {
            override fun eval(context: ScriptContext): Any = compiled.eval(context)

            override fun getEngine(): ScriptEngine = this@FIXNashornScriptEngine
        }
    }

    override fun <T : Any> getInterface(clasz: Class<T>): T? {
        throw UnsupportedOperationException("getInterface is not supported by FIXNashornScriptEngine")
    }

    override fun <T : Any> getInterface(thiz: Any, clasz: Class<T>): T? {
        throw UnsupportedOperationException("getInterface is not supported by FIXNashornScriptEngine")
    }

    override fun invokeMethod(thiz: Any, name: String, vararg args: Any?): Any? {
        return baseEngine.invokeMethod(thiz, name, *args)
    }

    override fun invokeFunction(name: String, vararg args: Any?): Any? {
        return baseEngine.invokeMethod(context.getBindings(ScriptContext.ENGINE_SCOPE), name, *args)
    }
}
