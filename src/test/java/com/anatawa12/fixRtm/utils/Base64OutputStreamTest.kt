package com.anatawa12.fixRtm.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class Base64OutputStreamTest {
    @ParameterizedTest
    @MethodSource("methodSourceProvider")
    fun test(expectBase64: String, testData: ByteArray) {
        assertEquals(expectBase64, buildString {
            Base64OutputStream(ToStringOutputStream(this)).use {
                it.write(testData)
            }
        })
    }

    companion object {
        @JvmStatic
        @Suppress("unused")
        fun methodSourceProvider(): List<Arguments> {
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
    }
}
