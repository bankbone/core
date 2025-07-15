package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.sharedkernel.domain.OutboxEvent
import com.bankbone.core.sharedkernel.ports.EventDeserializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.slf4j.LoggerFactory

class KotlinxEventDeserializer : EventDeserializer {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val json = Json {
        classDiscriminator = "type"
        serializersModule = SerializersModule {
            polymorphic(DomainEvent::class) {
                subclass(LedgerTransactionPosted::class)
            }
        }
    }

    override fun deserialize(outboxEvent: OutboxEvent): DomainEvent? {
        return try {
            json.decodeFromString<DomainEvent>(outboxEvent.payload)
        } catch (e: Exception) {
            logger.error("Failed to deserialize event ID ${outboxEvent.id} of type ${outboxEvent.eventType}", e)
            null
        }
    }
}