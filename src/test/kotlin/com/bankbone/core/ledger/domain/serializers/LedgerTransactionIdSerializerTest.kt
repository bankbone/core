package com.bankbone.core.ledger.domain.serializers

import com.bankbone.core.ledger.domain.LedgerTransaction
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class LedgerTransactionIdSerializerTest {

    @Test
    fun `should serialize LedgerTransactionId to UUID string`() {
        val uuid = UUID.randomUUID()
        val id = LedgerTransaction.Id(uuid)
        val expectedJson = "\"$uuid\""
        val actualJson = Json.encodeToString(LedgerTransactionIdSerializer, id)
        assertEquals(expectedJson, actualJson)
    }

    @Test
    fun `should deserialize UUID string to LedgerTransactionId`() {
        val uuid = UUID.randomUUID()
        val json = "\"$uuid\""
        val expectedId = LedgerTransaction.Id(uuid)
        val actualId = Json.decodeFromString(LedgerTransactionIdSerializer, json)
        assertEquals(expectedId, actualId)
    }
}