package com.bankbone.core.sharedkernel.infrastructure

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class InstantSerializerTest {

    @Test
    fun `should serialize Instant to ISO-8601 string`() {
        val value = Instant.parse("2024-07-21T14:30:00Z")
        val expectedJson = "\"2024-07-21T14:30:00Z\""
        val actualJson = Json.encodeToString(InstantSerializer, value)
        assertEquals(expectedJson, actualJson)
    }

    @Test
    fun `should deserialize ISO-8601 string to Instant`() {
        val json = "\"2024-07-21T14:30:00Z\""
        val expectedValue = Instant.parse("2024-07-21T14:30:00Z")
        val actualValue = Json.decodeFromString(InstantSerializer, json)
        assertEquals(expectedValue, actualValue)
    }
}