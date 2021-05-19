package com.anatawa12.fixRtm

import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.common.Loader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class UtilsKtTest {
    companion object {
        init {
            Loader.injectData(
                ForgeVersion.majorVersion.toString(), // major
                ForgeVersion.minorVersion.toString(), // minor
                ForgeVersion.revisionVersion.toString(), // rev
                ForgeVersion.buildVersion.toString(), // build
                ForgeVersion.mcVersion, // mccversion
                ForgeVersion.mcpVersion, // mcpversion
                File("eclipse"), // minecraftDir
                mutableListOf<Nothing>(), // injectedContainers
            )
            Loader::class.java.getDeclaredMethod("initializeLoader").also { it.isAccessible = true }
                .invoke(Loader.instance())
        }
    }

    @Test
    fun joinLinesForJsonReading() {
        val validJson = """
            {
                "integer": 10,
                "number": 0.0,
                "number-expo": 0.0e+0,
                "signed-number": -0.0e+0,
                "string": "some string",
                "escaped": "\"\r\n",
                "end": 0
            }
        """.trimIndent()
        val validJsonTokens = """
            "integer": 0,
            "number": 0.0,
            "number-expo": 0.0e+0,
            "signed-number": -0.0e+0,
            "string": "some string",
            "escaped": "\"\r\n",
            "end": 0,
        """.trimIndent()
        val invalidJson = """
            {
                "integer": 1
            0,
                "number": 0.
            0,
                "number-expo": 0.0
            e+0,
                "signed-number": -
            0.0e+0,
                "string": "some
             string",
                "escaped": "\
            "\r\n",
                "end": 0
            }
        """.trimIndent()

        assertEquals(validJson, joinLinesForJsonReading(validJson.lines()))
        assertEquals(validJsonTokens, joinLinesForJsonReading(validJsonTokens.lines()))
        assertEquals(validJson, joinLinesForJsonReading(invalidJson.lines()))
    }
}
