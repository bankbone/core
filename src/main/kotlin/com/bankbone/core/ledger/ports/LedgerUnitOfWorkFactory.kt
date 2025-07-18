package com.bankbone.core.ledger.ports

interface LedgerUnitOfWorkFactory {
    fun create(): LedgerUnitOfWork
}