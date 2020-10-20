package com.anatawa12.fixRtm.scripting.rhino

import jdk.nashorn.api.scripting.JSObject
import org.mozilla.javascript.*
import org.mozilla.javascript.Function

class NativeJSObject(
    scope: Scriptable?,
    javaObject: JSObject
) : NativeJavaObject(scope, javaObject, null), Function, Wrapper {
    private val body: JSObject get() = unwrap() as JSObject

    // Scriptable
    override fun getClassName(): String = body.className

    override fun get(name: String, start: Scriptable?): Any? {
        return wrapToR(Context.getCurrentContext(), this, body.getMember(name))
    }

    override fun get(index: Int, start: Scriptable?): Any? {
        return wrapToR(Context.getCurrentContext(), this, body.getSlot(index))
    }

    override fun has(name: String, start: Scriptable?): Boolean = body.hasMember(name)

    override fun has(index: Int, start: Scriptable?): Boolean = body.hasSlot(index)

    override fun put(name: String, start: Scriptable?, value: Any?) {
        body.setMember(name, wrapToN(value))
    }

    override fun put(index: Int, start: Scriptable?, value: Any?) {
        body.setSlot(index, wrapToN(value))
    }

    override fun delete(name: String?) = body.removeMember(name)

    override fun delete(index: Int) = delete("$index")

    override fun getIds() = body.keySet().toTypedArray<Any>()

    override fun hasInstance(instance: Scriptable?): Boolean = body.isInstance(instance)

    override fun call(cx: Context, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any>): Any? {
        return wrapToR(cx, scope, body.call(thisObj, Array(args.size) { wrapToN(args[it]) }))
    }

    override fun construct(cx: Context, scope: Scriptable?, args: Array<out Any>): Scriptable? {
        return wrapNewObjectToR(cx, scope, body.newObject(Array(args.size) { wrapToN(args[it]) }))
    }
}

class JSObjectImpl(val body: Scriptable) : JSObject {
    override fun call(p0: Any?, vararg p1: Any?): Any? {
        val callable = body as Callable
        val cx = Context.getCurrentContext() ?: error("not in context")
        val result = callable.call(
            cx,
            body,
            wrapNewObjectToR(cx, body, p0),
            Array(p1.size) { wrapToR(cx, body, p1[it]) }
        )
        return wrapToN(result)
    }

    override fun newObject(vararg p0: Any?): Any? {
        val callable = body as Function
        val cx = Context.getCurrentContext() ?: error("not in context")
        val result = callable.construct(
            cx,
            body,
            Array(p0.size) { wrapToR(cx, body, p0[it]) }
        )
        return wrapToN(result)
    }

    override fun eval(p0: String?): Any? {
        val cx = Context.getCurrentContext() ?: error("not in context")
        return wrapToN(ScriptRuntime.evalSpecial(cx, body, body, emptyArray(), "<eval>", -1))
    }

    override fun getMember(p0: String?): Any? {
        return body.get(p0, body)
    }

    override fun getSlot(p0: Int): Any? {
        return body.get(p0, body)
    }

    override fun hasMember(p0: String?): Boolean {
        return body.has(p0, body)
    }

    override fun hasSlot(p0: Int): Boolean {
        return body.has(p0, body)
    }

    override fun removeMember(p0: String?) {
        return body.delete(p0)
    }

    override fun setMember(p0: String?, p1: Any?) {
        return body.put(p0, body, p1)
    }

    override fun setSlot(p0: Int, p1: Any?) {
        return body.put(p0, body, p1)
    }

    override fun keySet(): MutableSet<String> = body.ids.mapTo(mutableSetOf()) { it.toString() }

    override fun values(): MutableCollection<Any?> = keySet().mapTo(mutableListOf()) { this.getMember(it) }

    override fun isInstance(p0: Any?): Boolean {
        val cx = Context.getCurrentContext()
        return body.hasInstance(wrapNewObjectToR(cx, body, p0))
    }

    override fun isInstanceOf(p0: Any?): Boolean {
        val cx = Context.getCurrentContext()
        return wrapNewObjectToR(cx, body, p0)?.hasInstance(body) ?: false
    }

    override fun getClassName(): String = body.className

    override fun isFunction(): Boolean = body is Function

    override fun isStrictFunction(): Boolean = isFunction

    override fun isArray(): Boolean = false

    override fun toNumber(): Double = body.getDefaultValue(Double::class.java) as Double
}

private fun wrapNewObjectToR(cx: Context, scope: Scriptable?, obj: Any?): Scriptable? {
    if (obj == null) return null
    return cx.wrapFactory.wrapNewObject(cx, scope, obj)
}

private fun wrapToR(cx: Context, scope: Scriptable?, obj: Any?): Any? {
    return cx.wrapFactory.wrap(cx, scope, obj, obj?.javaClass)
}

private fun wrapToN(obj: Any?): Any? {
    val obj = if (obj is Wrapper) obj.unwrap() else obj
    if (obj == null) return null
    if (obj is JSObject) return obj
    if (obj is Scriptable) return JSObjectImpl(obj)
    return obj
}
