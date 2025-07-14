package com.bankbone.core.ledger.application

import com.bankbone.core.ledger.domain.LedgerEntry
import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.ports.ChartOfAccountsRepository
import com.bankbone.core.ledger.ports.LedgerTransactionRepository
import com.bankbone.core.sharedkernel.ports.DomainEventPublisher
import java.util.*

class LedgerPostingService(
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val chartOfAccountsRepository: ChartOfAccountsRepository,
    private val domainEventPublisher: DomainEventPublisher
) {
    suspend fun createAndPostTransaction(
        sourceTransactionId: String,
        description: String,
        entries: List<LedgerEntry>
    ): LedgerTransaction {
        if (entries.isNotEmpty()) {
            val accountIds = entries.map { it.accountId }.toSet()
            val foundAccounts = chartOfAccountsRepository.findAllByIds(accountIds)
            if (foundAccounts.size != accountIds.size) {
                val foundAccountIds = foundAccounts.map { it.id }.toSet()
                val missingAccountIds = accountIds - foundAccountIds
                throw IllegalArgumentException("Accounts do not exist or are not active in the Chart of Accounts: ${missingAccountIds.joinToString(", ")}")
            }
        }

        val transaction = LedgerTransaction(
            id = UUID.randomUUID().toString(),
            sourceTransactionId = sourceTransactionId,
            description = description,
            entries = entries
        )

        ledgerTransactionRepository.save(transaction)

        // Publish domain events after the transaction is successfully saved
        domainEventPublisher.publish(transaction.domainEvents())
        transaction.clearEvents()

        return transaction
    }
}
