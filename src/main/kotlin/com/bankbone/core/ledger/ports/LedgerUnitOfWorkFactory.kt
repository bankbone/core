package com.bankbone.core.ledger.ports

/**
 * A factory responsible for creating instances of a Unit of Work.
 */
interface LedgerUnitOfWorkFactory {
    fun create(): LedgerUnitOfWork
}