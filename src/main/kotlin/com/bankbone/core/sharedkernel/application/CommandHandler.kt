package com.bankbone.core.sharedkernel.application

/**
 * A generic interface for a handler that executes a command and returns a result.
 * This serves as the core abstraction for our use cases and decorators.
 */
interface CommandHandler<C, R> {
    suspend fun handle(command: C): R
}