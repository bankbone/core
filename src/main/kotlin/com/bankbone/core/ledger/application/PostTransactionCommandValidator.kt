package com.bankbone.core.ledger.application

import com.bankbone.core.ledger.application.commands.PostTransactionCommand
import com.bankbone.core.ledger.ports.ChartOfAccountsRepository

class PostTransactionCommandValidator {
    suspend fun validate(command: PostTransactionCommand, chartOfAccountsRepository: ChartOfAccountsRepository) {
        // 1. Basic command validation
        require(command.sourceTransactionId.isNotBlank()) { "Source transaction ID must not be blank." }
        require(command.description.isNotBlank()) { "Description must not be blank." }

        // 2. Business rule validation (checking account existence)
        if (command.entries.isNotEmpty()) {
            val accountIds = command.entries.map { it.accountId }.toSet()
            val foundAccounts = chartOfAccountsRepository.findAllByIds(accountIds)
            if (foundAccounts.size != accountIds.size) {
                val foundAccountIds = foundAccounts.map { it.id }.toSet()
                val missingAccountIds = accountIds - foundAccountIds
                throw IllegalArgumentException("Accounts do not exist or are not active in the Chart of Accounts: ${missingAccountIds.joinToString(", ")}")
            }
        }
    }
}