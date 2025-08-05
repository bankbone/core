package com.bankbone.core.sharedkernel.application

import com.bankbone.core.sharedkernel.domain.IdempotencyKey

/**
 * An interface for commands that support idempotency, providing a key for tracking.
 */
interface IdempotentCommand {
    val idempotencyKey: IdempotencyKey
}