package com.bankbone.core.sharedkernel.ports

import java.io.Closeable

// Represents a single business transaction.
interface UnitOfWork : Closeable {
    suspend fun <R> executeInTransaction(block: suspend () -> R): R
    suspend fun commit()
    suspend fun rollback()
}