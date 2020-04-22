package com.anatawa12.fixRtm.asm.config

import java.lang.Appendable
import java.lang.StringBuilder

class KVSConfig {
    private val entries = mutableListOf<Entry>()
    private val errors = mutableListOf<Error>()

    private fun getSingleEntry(name: String, comment: String, default: String): Entry.Prop? {
        val nameed = entries.asSequence()
                .filterIsInstance<Entry.Prop>()
                .filter { it.name == name }
                .toList()

        if (nameed.isEmpty()) {
            val entry = Entry.Prop(name, computeComment(comment, default), default)
            if (entries.last() != Entry.EmptyLine)
                entries.add(Entry.EmptyLine)
            entries.add(entry)
            return entry
        } else if (nameed.size == 1) {
            return nameed[0]
        } else {
            errors.add(Error(name, "'$name' have to have only one."))
            return null
        }
    }

    private fun getMultiEntry(name: String, comment: String): List<Entry.Prop> {
        return entries.asSequence()
                .filterIsInstance<Entry.Prop>()
                .filter { it.name == name }
                .toList()
    }

    private fun computeComment(comment: String, default: String): String = "${comment}\ndefault: $default"
            .lineSequence()
            .map { " $it" }
            .joinToString("\n")

    fun loadFile(lines: Sequence<String>) {
        val commentLines = mutableListOf<String>()
        for ((index, line) in lines
                .let { DropLastEmptyLineSequence(it) }
                .withIndex()) {
            if (line.getOrNull(0) == '#') {
                commentLines.add(line.substring(1))
                continue
            }

            if (line.isBlank()) {
                if (commentLines.isNotEmpty())
                    entries.add(Entry.Comment(commentLines.joinToString("\n")))
                entries.add(Entry.EmptyLine)
                commentLines.clear()
                continue
            }

            val equalIndex = line.indexOf('=')
            if (equalIndex == -1) throw Exception("invalid line at ${index + 1}: not contains '='")

            val name = line.substring(0, equalIndex)
            val value = line.substring(equalIndex + 1)
            entries.add(Entry.Prop(name, commentLines.joinToString("\n"), value))
            commentLines.clear()
        }
        if (commentLines.isNotEmpty()) {
            entries.add(Entry.Comment(commentLines.joinToString("\n")))
        }
    }

    fun writeTo(appendable: Appendable) {
        for (entry in entries) {
            when (entry) {
                Entry.EmptyLine -> {
                    appendable.appendln()
                }
                is Entry.Comment -> {
                    for (commentLine in entry.comment.lineSequence()) {
                        appendable.append('#').appendln(commentLine)
                    }
                }
                is Entry.Prop -> {
                    for (commentLine in entry.comment.lineSequence()) {
                        appendable.append('#').appendln(commentLine)
                    }
                    appendable.append(entry.name).append('=').appendln(entry.value)
                }
            }
        }
    }

    fun throwSyntaxErrors(fileName: String) {
        if (errors.isNotEmpty()) {
            val lines = mutableListOf("some errors in config file: $fileName")
            for (error in errors) {
                lines.add("property ${error.name}: ${error.message}")
            }
            throw Exception(lines.joinToString(System.lineSeparator()))
        }
    }

    private sealed class Entry {
        object EmptyLine : Entry()
        class Comment(val comment: String): Entry()
        class Prop(val name: String, val comment: String, var value: String): Entry()
    }
    //private class Entry(val name: String?, val comment: String, var value: String)
    private class Error(val name: String, val message: String)

    private class DropLastEmptyLineSequence(val base: Sequence<String>) : Sequence<String> {
        override fun iterator(): Iterator<String> = iterator<String> {
            var lastLine: String? = null
            for (s in base) {
                if (lastLine != null)
                    yield(lastLine)
                lastLine = s
            }
            if (!lastLine.isNullOrEmpty()) {
                yield(lastLine)
            }
        }
    }
}
