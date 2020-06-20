package com.anatawa12.fixRtm.asm

import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.ClassCompiler_compileToClassFiles_desc
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.ClassCompiler_compileToClassFiles_name
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.ClassCompiler_name
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.CodeGenHooks_implDecompile_desc
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.CodeGenHooks_implDecompile_name
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.CodeGenHooks_internal
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.Codegen_generateNativeFunctionOverrides_desc
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.Codegen_generateNativeFunctionOverrides_name
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.Codegen_internal
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.Codegen_name
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.Codegen_scriptOrFnNodes_desc
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.Codegen_scriptOrFnNodes_name
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.Codegen_sourceString_desc
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.Codegen_sourceString_name
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.NativeFunction_decompile_desc
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.NativeFunction_decompile_name
import com.anatawa12.fixRtm.scripting.rhino.CodeGenHooks.NativeFunction_name
import com.anatawa12.fixRtm.scripting.rhino.PrimitiveJavaHelper
import com.anatawa12.fixRtm.scripting.rhino.RhinoHooks
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*

class RhinoTransformer : IClassTransformer {
    override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
        if (name == RhinoHooks.className) {
            val reader = ClassReader(basicClass!!)
            val writer = ClassWriter(0)
            reader.accept(RhinoHooksVisitor(writer), 0)
            return writer.toByteArray()
        } else if (name == NativeJavaObject_name) {
            val reader = ClassReader(basicClass!!)
            val writer = ClassWriter(0)
            reader.accept(NativeJavaObjectVisitor(writer), 0)
            return writer.toByteArray()
        } else if (name == PrimitiveJavaHelper.NativeString_name) {
            val reader = ClassReader(basicClass!!)
            val writer = ClassWriter(0)
            reader.accept(NativeStringVisitor(writer), 0)
            return writer.toByteArray()
        } else if (name == NativeFunction_name) {
            val reader = ClassReader(basicClass!!)
            val writer = ClassWriter(0)
            reader.accept(NativeFunctionVisitor(writer), 0)
            return writer.toByteArray()
        } else if (name == Codegen_name) {
            val reader = ClassReader(basicClass!!)
            val writer = ClassWriter(0)
            reader.accept(CodegenVisitor(writer), 0)
            return writer.toByteArray()
        } else if (name == ClassCompiler_name) {
            val reader = ClassReader(basicClass!!)
            val writer = ClassWriter(0)
            reader.accept(ClassCompilerVisitor(writer), 0)
            return writer.toByteArray()
        } else if (name == NativeJavaClass_name) {
            val reader = ClassReader(basicClass!!)
            val writer = ClassWriter(0)
            reader.accept(NativeJavaClassVisitor(writer), 0)
            return writer.toByteArray()
        }
        return basicClass
    }

    class RhinoHooksVisitor(visitor: ClassVisitor): ClassVisitor(ASM5, visitor) {
        override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            var mv: MethodVisitor
            mv = super.visitMethod(access, name, desc, signature, exceptions)
            mv = ReplaceCallingGetSizeRank(mv)
            return mv
        }
    }

    class ReplaceCallingGetSizeRank(visitor: MethodVisitor) : MethodVisitor(ASM5, visitor) {
        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
            if (owner == RhinoHooks.internalClassName
                    && name == RhinoHooks.getSizeRank_name
                    && desc == RhinoHooks.getSizeRank_desc) {
                super.visitMethodInsn(INVOKESTATIC, NativeJavaObject_internal, RhinoHooks.getSizeRank_name,
                        RhinoHooks.getSizeRank_desc, false)
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf)
            }
        }
    }

    class NativeJavaObjectVisitor(visitor: ClassVisitor): ClassVisitor(ASM5, visitor) {
        private var getSizeRank_flag = false
        private var coerceTypeImpl_flag = false
        private var getConversionWeight_flag = false

        override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            var mv: MethodVisitor
            if (name == RhinoHooks.getSizeRank_name && desc == RhinoHooks.getSizeRank_desc) {
                getSizeRank_flag = true
                var access = access
                access = access and (ACC_PUBLIC or ACC_PRIVATE or ACC_PROTECTED).inv()
                access = access or ACC_PUBLIC
                mv = super.visitMethod(access, name, desc, signature, exceptions)
            } else if (name == RhinoHooks.coerceTypeImpl_name && desc == RhinoHooks.coerceTypeImpl_desc) {
                coerceTypeImpl_flag = true
                // that's static
                mv = InsertCodeAtFirstVisitor(super.visitMethod(access, name, desc, signature, exceptions)) {
                    val ifeq = Label()

                    visitVarInsn(ALOAD, 0) // class
                    visitVarInsn(ALOAD, 1) // object
                    visitMethodInsn(INVOKESTATIC, RhinoHooks.internalClassName,
                            RhinoHooks.coerceTypeImpl_name, RhinoHooks.coerceTypeImpl_desc, false)
                    visitInsn(DUP)
                    visitFieldInsn(GETSTATIC, CoerceTypeImplFailed_internal,
                            "INSTANCE", "L${CoerceTypeImplFailed_internal};")
                    visitJumpInsn(IF_ACMPEQ, ifeq)
                    visitInsn(ARETURN)
                    visitLabel(ifeq)
                    visitFrame(F_SAME1, 0, null, 1, arrayOf("java/lang/Object"))
                    visitInsn(POP)
                }
            } else if (name == RhinoHooks.getConversionWeight_name && desc == RhinoHooks.getConversionWeight_desc) {
                getConversionWeight_flag = true
                // that's static
                mv = InsertCodeAtFirstVisitor(super.visitMethod(access, name, desc, signature, exceptions)) {
                    val ifeq = Label()

                    visitVarInsn(ALOAD, 0) // object
                    visitVarInsn(ALOAD, 1) // class
                    visitMethodInsn(INVOKESTATIC, RhinoHooks.internalClassName,
                            RhinoHooks.getConversionWeight_name, RhinoHooks.getConversionWeight_desc, false)
                    visitInsn(DUP)
                    visitLdcInsn(99)
                    visitJumpInsn(IF_ICMPEQ, ifeq)
                    visitInsn(IRETURN)
                    visitLabel(ifeq)
                    visitFrame(F_SAME1, 0, null, 1, arrayOf(INTEGER))
                    visitInsn(POP)

                }
            } else {
                mv = super.visitMethod(access, name, desc, signature, exceptions)
            }
            return mv
        }

        override fun visitEnd() {
            check(getSizeRank_flag) { "getSizeRank is not transformed" }
            check(coerceTypeImpl_flag) { "coerceTypeImpl is not transformed" }
            check(getConversionWeight_flag) { "getConversionWeight is not transformed" }
            super.visitEnd()
        }
    }

    class NativeStringVisitor(visitor: ClassVisitor): ClassVisitor(ASM5, visitor) {
        override fun visitEnd() {
            visitMethod(ACC_PUBLIC, PrimitiveJavaHelper.get_name, PrimitiveJavaHelper.get_desc,
                    null, null).apply {
                visitCode()
                visitVarInsn(ALOAD, 0) // this
                visitVarInsn(ALOAD, 1) // name
                visitVarInsn(ALOAD, 2) // start
                visitMethodInsn(INVOKESPECIAL, IdScriptableObject_internal, PrimitiveJavaHelper.get_name, PrimitiveJavaHelper.get_desc, false)
                visitInsn(DUP)
                visitFieldInsn(GETSTATIC, Scriptable_internal, Scriptable_NOT_FOUND_name, Scriptable_NOT_FOUND_desc)
                val ifEq = Label()
                visitJumpInsn(IF_ACMPEQ, ifEq)
                visitInsn(ARETURN)
                visitLabel(ifEq)
                visitFrame(F_SAME1, 0, null, 1, arrayOf("java/lang/Object"))
                visitInsn(POP)
                visitVarInsn(ALOAD, 1) // name
                visitVarInsn(ALOAD, 2) // start
                visitMethodInsn(INVOKESTATIC, PrimitiveJavaHelper.internalClassName, PrimitiveJavaHelper.NativeString_get_hook_name, PrimitiveJavaHelper.NativeString_get_hook_desc, false)
                visitInsn(ARETURN)
                visitMaxs(3, 3)
                visitEnd()
            }
            super.visitEnd()
        }
    }

    class NativeFunctionVisitor(visitor: ClassVisitor): ClassVisitor(ASM5, visitor) {
        override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            if (name == NativeFunction_decompile_name && desc == NativeFunction_decompile_desc) {
                return super.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions)
            }
            return super.visitMethod(access, name, desc, signature, exceptions)
        }
    }

    class CodegenVisitor(visitor: ClassVisitor): ClassVisitor(ASM5, visitor) {
        override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            var mv = super.visitMethod(access, name, desc, signature, exceptions)
            if (name == Codegen_generateNativeFunctionOverrides_name && desc == Codegen_generateNativeFunctionOverrides_desc) {
                mv = InsertCodeAtFirstVisitor(mv) {
                    visitVarInsn(ALOAD, 1)
                    visitVarInsn(ALOAD, 0)
                    visitFieldInsn(GETFIELD, Codegen_internal, Codegen_scriptOrFnNodes_name, Codegen_scriptOrFnNodes_desc)
                    visitVarInsn(ALOAD, 0)
                    visitFieldInsn(GETFIELD, Codegen_internal, Codegen_sourceString_name, Codegen_sourceString_desc)
                    visitMethodInsn(INVOKESTATIC, CodeGenHooks_internal, CodeGenHooks_implDecompile_name, CodeGenHooks_implDecompile_desc, false)
                }
            }
            return mv
        }

        override fun visitEnd() {
            visitField(ACC_PUBLIC, Codegen_sourceString_name, Codegen_sourceString_desc, null, null).apply {
                visitEnd()
            }
            super.visitEnd()
        }
    }

    class ClassCompilerVisitor(visitor: ClassVisitor): ClassVisitor(ASM5, visitor) {
        override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            var mv = super.visitMethod(access, name, desc, signature, exceptions)
            if (name == ClassCompiler_compileToClassFiles_name && desc == ClassCompiler_compileToClassFiles_desc) {
                mv = ClassCompilerCompileToClassFilesVisitor(mv)
            }
            return mv
        }
    }

    class ClassCompilerCompileToClassFilesVisitor(mv: MethodVisitor) : MethodVisitor(ASM5, mv) {
        override fun visitVarInsn(opcode: Int, `var`: Int) {
            super.visitVarInsn(opcode, `var`)
            if (opcode == ASTORE && `var` == 13) {
                visitVarInsn(ALOAD, 13)
                visitVarInsn(ALOAD, 1)
                visitFieldInsn(PUTFIELD, Codegen_internal, Codegen_sourceString_name, Codegen_sourceString_desc)
            }
        }

    }

    class NativeJavaClassVisitor(visitor: ClassVisitor): ClassVisitor(ASM5, visitor) {
        override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
            var mv = super.visitMethod(access, name, desc, signature, exceptions)
            if (name == NativeJavaClass_has_name && desc == NativeJavaClass_has_desc) {
                val returnTrue = Label()

                mv.visitCode()

                mv.visitVarInsn(ALOAD, 0)
                mv.visitFieldInsn(GETFIELD, NativeJavaClass_internal, "members", "L$JavaMembers_internal;")
                mv.visitVarInsn(ALOAD, 1)
                mv.visitLdcInsn(1)
                mv.visitMethodInsn(INVOKEVIRTUAL, JavaMembers_internal, "has", "(L$String_internal;Z)Z", false)
                mv.visitJumpInsn(IFNE, returnTrue)

                mv.visitLdcInsn("__javaObject__")
                mv.visitVarInsn(ALOAD, 1)
                mv.visitMethodInsn(INVOKEVIRTUAL, String_internal, Object_equals_name, Object_equals_desc, false)
                mv.visitJumpInsn(IFNE, returnTrue)

                mv.visitLdcInsn("class")
                mv.visitVarInsn(ALOAD, 1)
                mv.visitMethodInsn(INVOKEVIRTUAL, String_internal, Object_equals_name, Object_equals_desc, false)
                mv.visitJumpInsn(IFNE, returnTrue)

                mv.visitLdcInsn(0)
                mv.visitInsn(IRETURN)

                mv.visitFrame(F_SAME, 0, null, 0, null)
                mv.visitLabel(returnTrue)

                mv.visitLdcInsn(1)
                mv.visitInsn(IRETURN)

                mv.visitMaxs(3, 3)
                mv.visitEnd()

                mv = null
            } else if (name == NativeJavaClass_get_name && desc == NativeJavaClass_get_desc) {
                mv = NativeJavaClassGetVisitor(mv)
            }
            return mv
        }
    }

    class NativeJavaClassGetVisitor(visitor: MethodVisitor) : MethodVisitor(ASM5, visitor) {
        var state = START

        override fun visitLdcInsn(cst: Any?) {
            if (cst == "__javaObject__") {
                state = AFTER_LDC_JavaObject
            }
            super.visitLdcInsn(cst)
        }

        override fun visitJumpInsn(opcode: Int, label: Label) {
            if (state == AFTER_LDC_JavaObject && opcode == IFEQ) {
                state = AFTER_IFEQ
                val ifNot = label
                val ifClass = Label()
                super.visitJumpInsn(IFNE, ifClass)
                super.visitLdcInsn("class")
                super.visitVarInsn(ALOAD , 1)
                super.visitMethodInsn(INVOKEVIRTUAL, String_internal, Object_equals_name, Object_equals_desc, false)
                super.visitJumpInsn(IFEQ, ifNot)
                super.visitLabel(ifClass)
                super.visitFrame(F_APPEND, 3, arrayOf("org/mozilla/javascript/Context", "org/mozilla/javascript/Scriptable", "org/mozilla/javascript/WrapFactory"), 0, null)
                return
            }
            super.visitJumpInsn(opcode, label)
        }

        override fun visitFrame(type: Int, nLocal: Int, local: Array<out Any>?, nStack: Int, stack: Array<out Any>?) {
            if (state == AFTER_IFEQ) {
                state = END
                super.visitFrame(F_SAME, 0, null, 0, null)
                return
            }
            super.visitFrame(type, nLocal, local, nStack, stack)
        }

        override fun visitEnd() {
            check(state == END) { "logic failed" }
            super.visitEnd()
        }

        companion object {
            private const val START = 0
            private const val AFTER_LDC_JavaObject = 1
            private const val AFTER_IFEQ = 2
            private const val END = 3
        }
    }

    class InsertCodeAtFirstVisitor(visitor: MethodVisitor, private val function: MethodVisitor.() -> Unit)
        : MethodVisitor(ASM5, visitor) {
        private var visited = false
        override fun visitCode() {
            super.visitCode()
            function()
            visited = true
        }

        override fun visitEnd() {
            check(visited) { "visit code is not called" }
            super.visitEnd()
        }
    }

    companion object {
        const val NativeJavaObject_name = "org.mozilla.javascript.NativeJavaObject"
        const val NativeJavaObject_internal = "org/mozilla/javascript/NativeJavaObject"
        const val CoerceTypeImplFailed_internal = "com/anatawa12/fixRtm/scripting/rhino/CoerceTypeImplFailed"
        const val IdScriptableObject_internal = "org/mozilla/javascript/IdScriptableObject"
        const val Scriptable_internal = "org/mozilla/javascript/Scriptable"
        const val Scriptable_NOT_FOUND_name = "NOT_FOUND"
        const val Scriptable_NOT_FOUND_desc = "L${"java/lang/Object"};"

        const val JavaMembers_internal = "org/mozilla/javascript/JavaMembers"

        const val NativeJavaClass_name = "org.mozilla.javascript.NativeJavaClass"
        const val NativeJavaClass_internal = "org/mozilla/javascript/NativeJavaClass"
        const val NativeJavaClass_has_name = "has"
        const val NativeJavaClass_has_desc = "(L${"java/lang/String"};L${"org/mozilla/javascript/Scriptable"};)Z"
        const val NativeJavaClass_get_name = "get"
        const val NativeJavaClass_get_desc = "(L${"java/lang/String"};L${"org/mozilla/javascript/Scriptable"};)L${"java/lang/Object"};"

        const val String_internal = "java/lang/String"
        const val Object_equals_name = "equals"
        const val Object_equals_desc = "(L${"java/lang/Object"};)Z"

    }
}
