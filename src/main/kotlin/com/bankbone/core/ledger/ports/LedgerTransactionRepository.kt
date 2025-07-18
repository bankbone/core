package com.bankbone.core.ledger.ports

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.domain.LedgerTransaction.Id

interface LedgerTransactionRepository {
    suspend fun save(transaction: LedgerTransaction)
    suspend fun findById(id: Id): LedgerTransaction?
}
