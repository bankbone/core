package com.bankbone.core.sharedkernel.infrastructure.serialization

import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.sharedkernel.domain.DomainEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

object JsonConfig {
    val eventJson = Json {
        classDiscriminator = "type"
        serializersModule = SerializersModule {
            polymorphic(DomainEvent::class) {
                // Register all domain event subtypes here
                subclass(LedgerTransactionPosted::class)
            }
        }
    }
}