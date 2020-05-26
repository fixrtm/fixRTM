package com.anatawa12.fixRtm.scripting

import org.mozilla.classfile.ByteCode
import org.mozilla.classfile.ByteCode.*
import org.mozilla.classfile.ClassFileWriter
import org.mozilla.classfile.ClassFileWriter.classNameToSignature
import org.mozilla.javascript.ast.ScriptNode

object CodeGenHooks {
    @JvmStatic
    fun implDecompile(
            cfw: ClassFileWriter,
            scriptOrFnNodes: Array<ScriptNode>,
            source: String?
    ) {
        if (source == null) return

        cfw.startMethod(NativeFunction_decompile_name, NativeFunction_decompile_desc, ClassFileWriter.ACC_PUBLIC)
        cfw.addVariableDescriptor("this", classNameToSignature(cfw.className), cfw.currentCodeOffset, 0)
        cfw.addVariableDescriptor("indent", "I", cfw.currentCodeOffset, 1)
        cfw.addVariableDescriptor("flags", "I", cfw.currentCodeOffset, 2)

        val callSuper = cfw.acquireLabel()

        cfw.addILoad(1)
        cfw.add(IFNE, callSuper) // indent must be 0, if not null, call super

        cfw.addILoad(2)
        cfw.add(IFNE, callSuper) // flags must be 0, if not null, call super

        cfw.addPush(source)

        val count = scriptOrFnNodes.size

        var switchStart = 0
        var switchStackTop = 0
        if (count > 1) {
            // Generate switch but only if there is more then one
            // script/function
            cfw.addLoadThis()
            cfw.add(GETFIELD, cfw.className, "_id", "I")

            // do switch from 1 .. count - 1 mapping 0 to the default case
            switchStart = cfw.addTableSwitch(1, count - 1)
        }

        for (i in 0 until count) {
            val n = scriptOrFnNodes[i]
            if (i == 0) {
                if (count > 1) {
                    cfw.markTableSwitchDefault(switchStart)
                    switchStackTop = cfw.stackTop.toInt()
                }
            } else {
                cfw.markTableSwitchCase(switchStart, i - 1,
                        switchStackTop)
            }

            cfw.addPush(n.absolutePosition)
            cfw.addPush(n.absolutePosition + n.length)
            cfw.addInvoke(INVOKEVIRTUAL,
                    "java/lang/String",
                    "substring",
                    "(II)Ljava/lang/String;")
            cfw.add(ARETURN)
        }

        cfw.markLabel(callSuper)

        cfw.addALoad(0)
        cfw.addILoad(1)
        cfw.addILoad(2)
        cfw.addInvoke(INVOKESPECIAL, NativeFunction_name, NativeFunction_decompile_name, NativeFunction_decompile_desc)
        cfw.add(ARETURN)

        cfw.stopMethod(3)
    }

    const val ClassCompiler_name = "org.mozilla.javascript.optimizer.ClassCompiler"
    const val ClassCompiler_internal = "org/mozilla/javascript/optimizer/ClassCompiler"
    const val ClassCompiler_compileToClassFiles_name = "compileToClassFiles"
    const val ClassCompiler_compileToClassFiles_desc = "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)[Ljava/lang/Object;"

    const val Codegen_name = "org.mozilla.javascript.optimizer.Codegen"
    const val Codegen_internal = "org/mozilla/javascript/optimizer/Codegen"
    const val Codegen_sourceString_name = "sourceString"
    const val Codegen_sourceString_desc = "L${"java/lang/String"};"
    const val Codegen_scriptOrFnNodes_name = "scriptOrFnNodes"
    const val Codegen_scriptOrFnNodes_desc = "[L${"org/mozilla/javascript/ast/ScriptNode"};"
    const val Codegen_generateNativeFunctionOverrides_name = "generateNativeFunctionOverrides"
    const val Codegen_generateNativeFunctionOverrides_desc = "(L${"org/mozilla/classfile/ClassFileWriter"};L${"java/lang/String"};)V"

    const val NativeFunction_name = "org.mozilla.javascript.NativeFunction"
    const val NativeFunction_internal = "org/mozilla/javascript/NativeFunction"
    const val NativeFunction_decompile_name = "decompile"
    const val NativeFunction_decompile_desc = "(II)L${"java/lang/String"};"

    const val CodeGenHooks_name = "com.anatawa12.fixRtm.scripting.CodeGenHooks"
    const val CodeGenHooks_internal = "com/anatawa12/fixRtm/scripting/CodeGenHooks"

    const val CodeGenHooks_implDecompile_name = "implDecompile"
    const val CodeGenHooks_implDecompile_desc = "(L${"org/mozilla/classfile/ClassFileWriter"};[L${"org/mozilla/javascript/ast/ScriptNode"};L${"java/lang/String"};)V"
}
