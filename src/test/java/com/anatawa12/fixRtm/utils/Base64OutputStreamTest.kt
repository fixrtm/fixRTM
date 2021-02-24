package com.anatawa12.fixRtm.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class Base64OutputStreamTest {
    private fun createBase64(
        testData: ByteArray,
        addPadding: Boolean = false,
        addNewLinePer64: Boolean = false,
    ): String {
        return buildString {
            Base64OutputStream(ToStringOutputStream(this), addPadding, addNewLinePer64).use {
                it.write(testData)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("testBasicSource")
    fun testBasic(expectBase64: String, testData: ByteArray) {
        assertEquals(expectBase64, createBase64(testData))
    }

    @ParameterizedTest
    @MethodSource("testWithPaddingProvider")
    fun testWithPadding(expectBase64: String, testData: ByteArray) {
        assertEquals(expectBase64, createBase64(testData, addPadding = true))
    }

    @Test
    fun testNewLinePer64WithoutPadding() {
        assertEquals("AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA\n" +
                "AAA", createBase64(ByteArray(50), addNewLinePer64 = true))
        assertEquals("AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA\n" +
                "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA\n" +
                "AAAAAA", createBase64(ByteArray(100), addNewLinePer64 = true))
    }

    @Test
    fun testNewLinePer64WithPadding() {
        assertEquals("AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA\n" +
                "AAA=", createBase64(ByteArray(50), addPadding = true, addNewLinePer64 = true))
        assertEquals("AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA\n" +
                "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA\n" +
                "AAAAAA==", createBase64(ByteArray(100), addPadding = true, addNewLinePer64 = true))
        assertEquals("AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAA=\n",
            createBase64(ByteArray(47), addPadding = true, addNewLinePer64 = true))
    }

    companion object {
        @JvmStatic
        @Suppress("unused")
        fun testBasicSource(): List<Arguments> {
            return listOf(
                arguments("", "".toByteArray()),
                arguments("Zg", "f".toByteArray()),
                arguments("Zm8", "fo".toByteArray()),
                arguments("Zm9v", "foo".toByteArray()),
                arguments("Zm9vYg", "foob".toByteArray()),
                arguments("Zm9vYmE", "fooba".toByteArray()),
                arguments("Zm9vYmFy", "foobar".toByteArray()),
            )
        }

        @JvmStatic
        @Suppress("unused")
        fun testWithPaddingProvider(): List<Arguments> {
            return listOf(
                arguments("", "".toByteArray()),
                arguments("Zg==", "f".toByteArray()),
                arguments("Zm8=", "fo".toByteArray()),
                arguments("Zm9v", "foo".toByteArray()),
                arguments("Zm9vYg==", "foob".toByteArray()),
                arguments("Zm9vYmE=", "fooba".toByteArray()),
                arguments("Zm9vYmFy", "foobar".toByteArray()),
            )
        }
    }
}
