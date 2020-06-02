package com.anatawa12.fixRtm.scripting.rhino

import com.anatawa12.fixRtm.scripting.baseScope
import org.mozilla.javascript.*

@Suppress("FunctionName")
object PrimitiveJavaHelper {
    const val internalClassName = "com/anatawa12/fixRtm/scripting/rhino/PrimitiveJavaHelper"

    const val NativeString_name = "org.mozilla.javascript.NativeString"
    const val NativeString_internal = "org/mozilla/javascript/NativeString"
    const val get_name = "get"
    const val get_desc = "(Lj${"ava/lang/String"};L${"org/mozilla/javascript/Scriptable"};)L${"java/lang/Object"};"
    const val NativeString_get_hook_name = "NativeString_get"
    const val NativeString_get_hook_desc = "(L${"java/lang/String"};L${"org/mozilla/javascript/Scriptable"};)L${"java/lang/Object"};"
    const val NativeString_string_name = "string"
    const val NativeString_string_desc = "L${"java/lang/CharSequence"};"

    @JvmStatic
    fun NativeString_get(name: String?, start: Scriptable?): Any? {
        if (name in StringMethod_names) return Scriptable.NOT_FOUND
        return wrapFunctionForString(stringObject.get(name, start))
    }

    private val stringObject = usingContext { NativeJavaObject(baseScope, "", null) }

    private fun wrapFunctionForString(scriptable: Any?): Any? {
        if (scriptable is NativeJavaMethod)
            return FixRTMWrappedFunction(scriptable, PrimitiveJavaHelper::getStringInstance)
        else
            return scriptable
    }

    @JvmStatic
    fun getStringInstance(scriptable: Scriptable) = FixRTMRhinoAccessor.getStringOfNativeString(scriptable)

    val StringMethod_names = setOf(
            "charAt", "charCodeAt", "codePointAt", "concat", "includes", "endsWith", "indexOf", "lastIndexOf",
            "localeCompare", "match", "normalize", "padEnd", "padStart", "quote", "repeat", "replace", "search",
            "slice", "split", "startsWith", "substr", "substring", "toLocaleLowerCase", "toLocaleUpperCase",
            "toLowerCase", "toSource", "toString", "toUpperCase", "trim", "trimStart", "trimLeft", "trimEnd",
            "trimRight", "valueOf", "anchor", "big", "blink", "bold", "fixed", "fontcolor", "fontsize", "italics",
            "link", "small", "strike", "sub", "sup"
    )

    fun load() {}
}
