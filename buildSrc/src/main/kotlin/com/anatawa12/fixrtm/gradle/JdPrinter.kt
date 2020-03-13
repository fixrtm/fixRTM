package com.anatawa12.fixrtm.gradle

import org.jd.core.v1.api.printer.Printer

class JdPrinter : Printer {
    private var indentationCount = 0
    private val sb: StringBuilder = java.lang.StringBuilder()

    override fun toString(): String {
        return sb.toString()
    }

    override fun start(maxLineNumber: Int, majorVersion: Int, minorVersion: Int) {}

    override fun end() {}

    override fun printText(text: String) {
        append(text)
    }

    override fun printNumericConstant(constant: String) {
        append(constant)
    }

    override fun printStringConstant(constant: String, ownerInternalName: String?) {
        append(constant)
    }

    override fun printKeyword(keyword: String) {
        append(keyword)
    }

    override fun printDeclaration(type: Int, internalTypeName: String, name: String, descriptor: String?) {
        append(name)
    }

    override fun printReference(type: Int, internalTypeName: String, name: String, descriptor: String?, ownerInternalName: String?) {
        append(name)
    }

    override fun indent() {
        indentationCount++
    }

    override fun unindent() {
        indentationCount--
    }

    var lineAppended = false

    override fun startLine(lineNumber: Int) {
        lineAppended = false
    }

    private fun append(value: String) {
        if (!lineAppended) {
            for (i in 0 until indentationCount) sb.append(indent)
            lineAppended = true
        }
        sb.append(value)
    }

    override fun endLine() {
        sb.appendln()
    }

    override fun extraLine(count: Int) {}
    override fun startMarker(type: Int) {}
    override fun endMarker(type: Int) {}

    companion object {
        private const val indent = "    "
    }
}
