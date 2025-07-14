package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.ports.LedgerTransactionRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryLedgerTransactionRepository : LedgerTransactionRepository {
    private val transactions = ConcurrentHashMap<String, LedgerTransaction>()

    override suspend fun save(transaction: LedgerTransaction) {
        transactions[transaction.id] = transaction
    }

    override suspend fun findById(id: String): LedgerTransaction? {
        return transactions[id]
    }
}
