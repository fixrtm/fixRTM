package com.anatawa12.fixrtm.gradle

import com.github.difflib.patch.Patch
import org.objectweb.asm.tree.*
import org.objectweb.asm.util.Printer

object AbstractInsnNodePatchPrinter {
    fun print(source: List<AbstractInsnNode>, patch: Patch<AbstractInsnNode>) = buildString {
        var insnIndex = 0
        for (delta in patch.deltas) {
            for (line in source.subList(insnIndex, delta.source.position)) {
                append(' ').printInsn(line)
            }
            insnIndex = delta.source.position + delta.source.lines.size
            for (line in delta.source.lines) {
                append('-').printInsn(line)
            }
            for (line in delta.target.lines) {
                append('+').printInsn(line)
            }
        }
        for (line in source.subList(insnIndex, source.size)) {
            append(' ').printInsn(line)
        }
    }


    private fun Appendable.printInsn(insn: AbstractInsnNode) {
        val insnName = Printer.OPCODES[insn.opcode]
        insn.run {
            when (this) {
                is FieldInsnNode -> {
                    appendln("$insnName $owner.$name $desc")
                }
                is IincInsnNode -> {
                    appendln("$insnName $`var` $incr")
                }
                is InsnNode -> {
                    appendln(insnName)
                }
                is IntInsnNode -> {
                    appendln("$insnName $operand")
                }
                is InvokeDynamicInsnNode -> {
                    appendln("$insnName $name$desc $bsm ${bsmArgs!!.contentToString()}")
                }
                is JumpInsnNode -> {
                    appendln("$insnName :Label")
                }
                is LdcInsnNode -> {
                    appendln("$insnName $cst")
                }
                is LookupSwitchInsnNode -> {
                    appendln("$insnName $keys")
                }
                is MethodInsnNode -> {
                    appendln("$insnName $owner.$name $desc $itf")
                }
                is MultiANewArrayInsnNode -> {
                    appendln("$insnName $desc $dims")
                }
                is TableSwitchInsnNode -> {
                    appendln("$insnName $min $max")
                }
                is TypeInsnNode -> {
                    appendln("$insnName $desc")
                }
                is VarInsnNode -> {
                    appendln("$insnName $`var`")
                }
                else -> error("Not yet implemented: ${insn.javaClass.simpleName}")
            }
        }
    }
}
