package com.bankbone.core.sharedkernel.ports

import java.io.Closeable

// Represents a single business transaction.
interface UnitOfWork : Closeable {
    suspend fun <R> transaction(block: suspend (uow: UnitOfWork) -> R): R
    suspend fun commit()
    suspend fun rollback()
}