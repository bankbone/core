package com.bankbone.core.ledger.application.usecases

import com.bankbone.core.ledger.application.commands.CreateAccountCommand
import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountId
import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.ledger.ports.transaction
import com.bankbone.core.sharedkernel.application.CommandHandler
import java.util.*

/**
 * Use case for creating a new account in the chart of accounts.
 *
 * @property uowFactory Factory for creating unit of work instances
 */
class CreateAccountUseCase(
    private val uowFactory: LedgerUnitOfWorkFactory
) : CommandHandler<CreateAccountCommand, Account> {
    
    /**
     * Handles the account creation command.
     *
     * @param command The command containing account details
     * @return The newly created account
     * @throws IllegalArgumentException if the account name is blank or parent account doesn't exist
     */
    override suspend fun handle(command: CreateAccountCommand): Account {
        require(command.name.isNotBlank()) { "Account name must not be blank." }

        return uowFactory.create().transaction { uow ->
            val repo = uow.chartOfAccountsRepository()

            val parentId = command.parentAccountId?.let { parentIdStr ->
                if (!repo.exists(parentIdStr)) {
                    throw IllegalArgumentException("Parent account with ID $parentIdStr does not exist.")
                }
                parentIdStr
            }

            val newAccount = Account(
                id = AccountId.random(),
                name = command.name,
                type = command.type,
                asset = command.asset,
                parentAccountId = parentId,
                metadata = command.metadata
            )

            repo.add(newAccount)
            newAccount
        }
    }
}