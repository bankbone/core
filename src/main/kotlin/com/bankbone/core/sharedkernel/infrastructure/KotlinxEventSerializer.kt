package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.sharedkernel.ports.EventSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class KotlinxEventSerializer : EventSerializer {

    private val json = Json {
        classDiscriminator = "type"
        serializersModule = SerializersModule {
            polymorphic(DomainEvent::class) {
                subclass(LedgerTransactionPosted::class)
            }
        }
    }

    override fun serialize(event: DomainEvent): String {
        return json.encodeToString(event)
    }
}