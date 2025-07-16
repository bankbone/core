package com.bankbone.core.sharedkernel.domain

import java.util.UUID

/**
 * Represents a unique key for idempotent operations.
 * Could be a UUID or a combination of relevant identifiers.
 */
data class IdempotencyKey(val value: String) {
    constructor() : this(UUID.randomUUID().toString())
}