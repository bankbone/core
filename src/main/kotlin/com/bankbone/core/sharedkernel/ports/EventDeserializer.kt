package com.bankbone.core.sharedkernel.ports

import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.sharedkernel.domain.OutboxEvent

interface EventDeserializer {
    fun deserialize(outboxEvent: OutboxEvent): DomainEvent?
}