package com.bankbone.core.ledger.application

import com.bankbone.core.ledger.application.commands.PostTransactionCommand
import com.bankbone.core.ledger.application.PostTransactionCommandValidator
import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.ports.LedgerTransactionRepository
import java.util.*

class LedgerPostingService(
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val validator: PostTransactionCommandValidator
) {
    suspend fun postTransaction(command: PostTransactionCommand): LedgerTransaction {
        // 1. Delegate validation to the dedicated validator class.
        validator.validate(command)

        // 2. The service's core responsibility: orchestrate the domain and repository.
        val transaction = LedgerTransaction(
            id = UUID.randomUUID().toString(),
            sourceTransactionId = command.sourceTransactionId,
            description = command.description,
            entries = command.entries
        )

        ledgerTransactionRepository.save(transaction)
        return transaction
    }
}
