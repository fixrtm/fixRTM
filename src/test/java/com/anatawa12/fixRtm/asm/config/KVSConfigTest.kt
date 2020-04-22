package com.anatawa12.fixRtm.asm.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KVSConfigTest {
    @Test
    fun restoreTest() {
        val file = """
            # comment
            property=value
            
            
            # comment after empty line
            
        """.trimIndent()
        val cfg = KVSConfig()
        cfg.loadFile(file.lineSequence())
        val out = StringBuilder()
        cfg.writeTo(out)
        assertEquals(file, out.toString())
    }
}
