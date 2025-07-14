package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.ports.EventDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class JacksonEventDeserializer : EventDeserializer {
    private val objectMapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    // In a real application, this might be populated via reflection or a service locator
    // to avoid hardcoding every event type.
    private val eventTypes = mapOf(
        "LedgerTransactionPosted" to LedgerTransactionPosted::class.java
    )

    override fun deserialize(outboxEvent: OutboxEvent): DomainEvent? {
        val eventClass = eventTypes[outboxEvent.eventType] ?: return null
        return objectMapper.readValue(outboxEvent.payload, eventClass)
    }
}