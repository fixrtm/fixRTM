package com.anatawa12.fixRtm.asm.preprocessing

import com.anatawa12.fixRtm.asm.Preprocessor
import com.anatawa12.fixRtm.asm.config.MainConfig
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*

class PreprocessorTransformer : IClassTransformer {
    companion object {
        private val inPreprocessor = Type.getInternalName(Preprocessor::class.java)
    }

    override fun transform(name: String, transformedName: String, basicClass: ByteArray?): ByteArray? {
        if (basicClass == null) return basicClass
        val transformedInternalName =  transformedName.replace('.', '/')
        if (transformedInternalName == inPreprocessor) return basicClass
        if (!transformedInternalName.startsWith("com/anatawa12/fixRtm")
                && !transformedInternalName.startsWith("jp/ngt")) return basicClass
        val reader = ClassReader(basicClass)
        val writer = ClassWriter(0)
        reader.accept(writer
                .let { ClassTransformer(it) }, 0)
        return writer.toByteArray()
    }

    private class ClassTransformer(visitor: ClassVisitor) : ClassVisitor(Opcodes.ASM5, visitor) {
        override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            return MethodTransformer(super.visitMethod(access, name, desc, signature, exceptions))
        }
    }

    private class MethodTransformer(val visitor: MethodVisitor) : MethodVisitor(Opcodes.ASM5, visitor) {
        override fun visitInsn(opcode: Int)
                = implInsn { super.visitInsn(opcode) }
        override fun visitIntInsn(opcode: Int, operand: Int)
                = implInsn { super.visitIntInsn(opcode, operand) }
        override fun visitVarInsn(opcode: Int, `var`: Int)
                = implInsn { super.visitVarInsn(opcode, `var`) }
        override fun visitTypeInsn(opcode: Int, type: String?)
                = implInsn { super.visitTypeInsn(opcode, type) }
        override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, desc: String?)
                = implInsn { super.visitFieldInsn(opcode, owner, name, desc) }
        override fun visitInvokeDynamicInsn(name: String?, desc: String?, bsm: Handle?, vararg bsmArgs: Any?)
                = implInsn { super.visitInvokeDynamicInsn(name, desc, bsm, *bsmArgs) }
        override fun visitJumpInsn(opcode: Int, label: Label?)
                = implInsn { super.visitJumpInsn(opcode, label) }
        override fun visitLabel(label: Label?)
                = implInsn { super.visitLabel(label) }
        override fun visitIincInsn(`var`: Int, increment: Int)
                = implInsn { super.visitIincInsn(`var`, increment) }
        override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?)
                = implInsn { super.visitTableSwitchInsn(min, max, dflt, *labels) }
        override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<Label?>?)
                = implInsn { super.visitLookupSwitchInsn(dflt, keys, labels) }
        override fun visitMultiANewArrayInsn(desc: String?, dims: Int)
                = implInsn { super.visitMultiANewArrayInsn(desc, dims) }
        override fun visitMaxs(maxStack: Int, maxLocals: Int)
                = implInsn { super.visitMaxs(maxStack, maxLocals) }

        private var lastLdc: Any? = NOT_INITED

        override fun visitMethodInsn(opcode: Int, owner: String?, name: String, desc: String?, itf: Boolean) {
            if (owner == inPreprocessor) {
                if (opcode != Opcodes.INVOKESTATIC)
                    throw RuntimeException("all methods in $inPreprocessor can be called only with INVOKESTATIC")
                if (desc != "(L${"java/lang/String"};)V")
                    throw RuntimeException("all methods in $inPreprocessor have single String param and returns void")
                val arg = lastLdc as? String ?: throw RuntimeException("all methods in $inPreprocessor have to call with string")
                when (name) {
                    Preprocessor::ifEnabled.name -> {
                        val enabled = MainConfig::class.java.getField(arg).get(null) as Boolean
                        mv = if (enabled) visitor else null
                    }
                    Preprocessor::ifDisabled.name -> {
                        val enabled = MainConfig::class.java.getField(arg).get(null) as Boolean
                        mv = if (enabled) null else visitor
                    }
                    Preprocessor::whatever.name -> {
                        mv = visitor
                    }
                    Preprocessor::never.name -> {
                        mv = null
                    }
                    else -> throw RuntimeException("method '$name' is not exits in $inPreprocessor")
                }
                lastLdc = NOT_INITED
            } else {
                onInsn()
                super.visitMethodInsn(opcode, owner, name, desc, itf)
            }
        }

        override fun visitLdcInsn(cst: Any?) = implInsn {
            lastLdc = cst
        }

        private inline fun <T> implInsn(main: () -> T): T {
            onInsn()
            return main()
        }

        private fun onInsn() {
            if (NOT_INITED != lastLdc) {
                super.visitLdcInsn(lastLdc)
                lastLdc = NOT_INITED
            }
        }

        companion object {
            private val NOT_INITED = Any()
        }
    }
}
