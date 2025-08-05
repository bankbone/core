package com.bankbone.core.sharedkernel.application.decorators

import com.bankbone.core.sharedkernel.application.CommandHandler
import com.bankbone.core.sharedkernel.application.IdempotentCommand
import com.bankbone.core.sharedkernel.application.IdempotentCommandHandler
import com.bankbone.core.sharedkernel.domain.IdempotencyKey
import com.bankbone.core.sharedkernel.ports.IdempotencyStore

/**
 * A decorator that adds idempotency to a command handler. It uses an IdempotencyStore
 * to prevent the re-execution of commands that have already been processed.
 */
class IdempotentCommandHandlerDecorator<C : IdempotentCommand, R>(
    private val decorated: CommandHandler<C, R>,
    idempotencyStore: IdempotencyStore
) : CommandHandler<C, R> {
    private val handler = object : IdempotentCommandHandler<C, R>(idempotencyStore) {
        override suspend fun extractIdempotencyKey(command: C): IdempotencyKey = command.idempotencyKey
    }

    override suspend fun handle(command: C): R = handler.handleIdempotently(command) { decorated.handle(command) }
}