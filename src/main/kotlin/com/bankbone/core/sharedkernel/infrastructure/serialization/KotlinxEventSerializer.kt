package com.bankbone.core.sharedkernel.infrastructure.serialization

import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.sharedkernel.ports.EventSerializer
import kotlinx.serialization.encodeToString

class KotlinxEventSerializer : EventSerializer {
    override fun serialize(domainEvent: DomainEvent): String {
        return JsonConfig.eventJson.encodeToString(domainEvent)
    }
}