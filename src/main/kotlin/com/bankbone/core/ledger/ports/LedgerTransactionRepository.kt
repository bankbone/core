package com.bankbone.core.ledger.ports

import com.bankbone.core.ledger.domain.LedgerTransaction

interface LedgerTransactionRepository {
    suspend fun save(transaction: LedgerTransaction)
    suspend fun findById(id: String): LedgerTransaction?
}
