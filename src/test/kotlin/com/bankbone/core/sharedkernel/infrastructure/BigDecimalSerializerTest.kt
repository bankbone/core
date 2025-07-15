package com.bankbone.core.sharedkernel.infrastructure

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class BigDecimalSerializerTest {

    @Test
    fun `should serialize BigDecimal to plain string`() {
        val value = BigDecimal("123456789.123456789")
        val expectedJson = "\"123456789.123456789\""
        val actualJson = Json.encodeToString(BigDecimalSerializer, value)
        assertEquals(expectedJson, actualJson)
    }

    @Test
    fun `should deserialize plain string to BigDecimal`() {
        val json = "\"123456789.123456789\""
        val expectedValue = BigDecimal("123456789.123456789")
        val actualValue = Json.decodeFromString(BigDecimalSerializer, json)
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `should handle whole numbers correctly`() {
        val value = BigDecimal("500")
        val expectedJson = "\"500\""
        val actualJson = Json.encodeToString(BigDecimalSerializer, value)
        assertEquals(expectedJson, actualJson)

        val deserialized = Json.decodeFromString(BigDecimalSerializer, actualJson)
        assertEquals(value, deserialized)
    }
}