package com.bankbone.core.sharedkernel.ports

import com.bankbone.core.sharedkernel.domain.DomainEvent

interface EventSerializer {
    fun serialize(event: DomainEvent): String
}