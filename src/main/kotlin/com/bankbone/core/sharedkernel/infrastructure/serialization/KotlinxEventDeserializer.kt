package com.bankbone.core.sharedkernel.infrastructure.serialization

import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.ports.EventDeserializer
import kotlinx.serialization.decodeFromString
import org.slf4j.LoggerFactory

class KotlinxEventDeserializer : EventDeserializer {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun deserialize(outboxEvent: OutboxEvent): DomainEvent? {
        return try {
            JsonConfig.eventJson.decodeFromString<DomainEvent>(outboxEvent.payload)
        } catch (e: Exception) {
            logger.error("Failed to deserialize event ID ${outboxEvent.id} of type ${outboxEvent.eventType}", e)
            null
        }
    }
}