package com.bankbone.core.sharedkernel.domain

import java.util.UUID

abstract class AggregateId(
    open val value: UUID
)
