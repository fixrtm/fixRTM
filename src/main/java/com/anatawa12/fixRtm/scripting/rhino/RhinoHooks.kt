package com.anatawa12.fixRtm.scripting.rhino

import jdk.nashorn.api.scripting.JSObject
import org.mozilla.javascript.*

object CoerceTypeImplFailed

/**
 * hooks used from Rhino by transformer
 */
object RhinoHooks {
    const val className = "com.anatawa12.fixRtm.scripting.rhino.RhinoHooks"
    const val internalClassName = "com/anatawa12/fixRtm/scripting/rhino/RhinoHooks"

    const val coerceTypeImpl_name = "coerceTypeImpl"
    const val coerceTypeImpl_desc = "(L${"java/lang/Class"};L${"java/lang/Object"};)L${"java/lang/Object"};"

    /**
     * hook of [NativeJavaObject.coerceTypeImpl]
     * if returns [CoerceTypeImplFailed], failed to coerce
     */
    @JvmStatic
    fun coerceTypeImpl(type: Class<*>, value: Any?): Any? {
        when (getJSTypeCode(value)) {
            JsTypeCode.UNDEFINED -> {
                if (type.isPrimitive && type != Boolean::class.java) {
                    when (type) {
                        Byte::class.javaPrimitiveType -> return 0.toByte()
                        Short::class.javaPrimitiveType -> return 0.toShort()
                        Int::class.javaPrimitiveType -> return 0.toInt()
                        Long::class.javaPrimitiveType -> return 0.toLong()
                        Float::class.javaPrimitiveType -> return Float.NaN
                        Double::class.javaPrimitiveType -> return Double.NaN
                    }
                }
            }
            JsTypeCode.NULL -> {
                if (type.isPrimitive && type != Boolean::class.java) {
                    when (type) {
                        Byte::class.javaPrimitiveType -> return 0.toByte()
                        Short::class.javaPrimitiveType -> return 0.toShort()
                        Int::class.javaPrimitiveType -> return 0.toInt()
                        Long::class.javaPrimitiveType -> return 0.toLong()
                        Float::class.javaPrimitiveType -> return 0.toFloat()
                        Double::class.javaPrimitiveType -> return 0.toDouble()
                    }
                }
            }
            JsTypeCode.BOOLEAN -> {
                if (type.isPrimitive && type != Boolean::class.java) {
                    val value = if (ScriptRuntime.toBoolean(value)) 1 else 0
                    when (type) {
                        Byte::class.javaPrimitiveType -> return value.toByte()
                        Short::class.javaPrimitiveType -> return value.toShort()
                        Int::class.javaPrimitiveType -> return value.toInt()
                        Long::class.javaPrimitiveType -> return value.toLong()
                        Float::class.javaPrimitiveType -> return value.toFloat()
                        Double::class.javaPrimitiveType -> return value.toDouble()
                    }
                }
            }
            JsTypeCode.NUMBER -> {
            }
            JsTypeCode.STRING -> {
            }
            JsTypeCode.JAVA_CLASS -> {
            }
            JsTypeCode.JAVA_OBJECT -> {
            }
            JsTypeCode.JAVA_ARRAY -> {
            }
            JsTypeCode.OBJECT -> {
            }
        }

        return CoerceTypeImplFailed
    }

    const val getConversionWeight_name = "getConversionWeight"
    const val getConversionWeight_desc = "(L${"java/lang/Object"};L${"java/lang/Class"};)I"

    /**
     * hook of [NativeJavaObject.getConversionWeight]
     * if returns non 99, will be converted with [coerceTypeImpl]
     */
    @JvmStatic
    fun getConversionWeight(value: Any?, to: Class<*>): Int {
        when (getJSTypeCode(value)) {
            JsTypeCode.UNDEFINED -> {
                if (to.isPrimitive && to != Boolean::class.javaPrimitiveType)
                    return 11 + getSizeRank(to)
            }
            JsTypeCode.NULL -> {
                if (to.isPrimitive && to != Boolean::class.javaPrimitiveType)
                    return 11 + getSizeRank(to)
            }
            JsTypeCode.BOOLEAN -> {
                if (to.isPrimitive && to != Boolean::class.javaPrimitiveType)
                    return 11 + getSizeRank(to)
            }
            JsTypeCode.NUMBER -> {
            }
            JsTypeCode.STRING -> {
            }
            JsTypeCode.JAVA_CLASS -> {
            }
            JsTypeCode.JAVA_OBJECT -> {
            }
            JsTypeCode.JAVA_ARRAY -> {
            }
            JsTypeCode.OBJECT -> {
            }
        }

        return 99 // can't by this
    }

    private enum class JsTypeCode {
        UNDEFINED,
        NULL,
        BOOLEAN,
        NUMBER,
        STRING,
        JAVA_CLASS,
        JAVA_OBJECT,
        JAVA_ARRAY,
        OBJECT,
    }

    private fun getJSTypeCode(value: Any?): JsTypeCode {
        return if (value == null) {
            JsTypeCode.NULL
        } else if (value === Undefined.instance) {
            JsTypeCode.UNDEFINED
        } else if (value is CharSequence) {
            JsTypeCode.STRING
        } else if (value is Number) {
            JsTypeCode.NUMBER
        } else if (value is Boolean) {
            JsTypeCode.BOOLEAN
        } else if (value is Scriptable) {
            if (value is NativeJavaClass) {
                JsTypeCode.JAVA_CLASS
            } else if (value is NativeJavaArray) {
                JsTypeCode.JAVA_ARRAY
            } else if (value is Wrapper) {
                JsTypeCode.JAVA_OBJECT
            } else {
                JsTypeCode.OBJECT
            }
        } else if (value is Class<*>) {
            JsTypeCode.JAVA_CLASS
        } else {
            val valueClass: Class<*> = value.javaClass
            if (valueClass.isArray) {
                JsTypeCode.JAVA_ARRAY
            } else JsTypeCode.JAVA_OBJECT
        }
    }

    const val getSizeRank_name = "getSizeRank"
    const val getSizeRank_desc = "(L${"java/lang/Class"};)I"

    /**
     * [NativeJavaObject.getSizeRank]
     */
    @JvmStatic
    private fun getSizeRank(aType: Class<*>?): Int = error("implement by transformer")

    const val wrapAsJavaObject_name = "wrapAsJavaObject"
    const val wrapAsJavaObject_desc = "(L${"org/mozilla/javascript/Scriptable"};L${"java/lang/Object"};" +
            "L${"java/lang/Class"};)L${"org/mozilla/javascript/Scriptable"};"

    @JvmStatic
    private fun wrapAsJavaObject(scope: Scriptable?, obj: Any, staticType: Class<*>): Scriptable? {
        if (obj is JSObjectImpl) return obj.body
        if (obj is JSObject) return NativeJSObject(scope, obj)
        return NativeJavaObject(scope, obj, staticType)
    }

    fun load() {}
}
