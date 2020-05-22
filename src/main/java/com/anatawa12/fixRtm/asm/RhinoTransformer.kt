package com.anatawa12.fixRtm.asm

import com.anatawa12.fixRtm.scripting.PrimitiveJavaHelper
import com.anatawa12.fixRtm.scripting.RhinoHooks
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
        const val CoerceTypeImplFailed_internal = "com/anatawa12/fixRtm/scripting/CoerceTypeImplFailed"
        const val IdScriptableObject_internal = "org/mozilla/javascript/IdScriptableObject"
        const val Scriptable_internal = "org/mozilla/javascript/Scriptable"
        const val Scriptable_NOT_FOUND_name = "NOT_FOUND"
        const val Scriptable_NOT_FOUND_desc = "L${"java/lang/Object"};"
    }
}
