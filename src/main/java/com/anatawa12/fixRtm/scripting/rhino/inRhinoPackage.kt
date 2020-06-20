@file:Suppress("PackageDirectoryMismatch")

package org.mozilla.javascript

class FixRTMWrappedFunction(private val javaFunction: NativeJavaMethod, private val getInstance: (Scriptable) -> Any?) : BaseFunction() {
    override fun getFunctionName(): String = javaFunction.functionName
    override fun decompile(indent: Int, flags: Int): String = javaFunction.decompile(indent, flags)
    override fun toString(): String = javaFunction.toString()

    override fun call(cx: Context, scope: Scriptable, thisObj: Scriptable?, args: Array<out Any>?): Any {
        return javaFunction.call(cx, scope, thisObj?.let { wrap(cx, scope, thisObj) }, args)
    }

    private fun wrap(cx: Context, scope: Scriptable, thisObj: Scriptable?): Scriptable? {
        if (thisObj == null) return null
        val topLevel = ScriptableObject.getTopLevelScope(scope)
        return cx.wrapFactory.wrapNewObject(cx, topLevel, getInstance(thisObj))
    }
}

object FixRTMRhinoAccessor {
    fun getStringOfNativeString(scriptable: Scriptable): String?
            = (scriptable as? NativeString)?.toCharSequence()?.toString()

}
