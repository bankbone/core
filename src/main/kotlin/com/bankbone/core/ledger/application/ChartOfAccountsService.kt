package com.bankbone.core.ledger.application

import com.bankbone.core.ledger.application.commands.CreateAccountCommand
import com.bankbone.core.ledger.application.commands.RenameAccountCommand
import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.ledger.ports.transaction
import com.bankbone.core.sharedkernel.application.IdempotentCommandHandler
import com.bankbone.core.sharedkernel.domain.IdempotencyKey
import com.bankbone.core.sharedkernel.ports.IdempotencyStore

class ChartOfAccountsService(
    private val uowFactory: LedgerUnitOfWorkFactory,
    idempotencyStore: IdempotencyStore
) : IdempotentCommandHandler<CreateAccountCommand, Account>(idempotencyStore) {

    override suspend fun extractIdempotencyKey(command: CreateAccountCommand): IdempotencyKey {
        return command.idempotencyKey
    }

    suspend fun createAccount(command: CreateAccountCommand): Account {
        require(command.name.isNotBlank()) { "Account name must not be blank." }

        return handleIdempotently(command) {
            uowFactory.create().transaction { uow ->
                val repo = uow.chartOfAccountsRepository()

                val parentId = command.parentAccountId?.let { Account.Id.fromString(it) }?.also {
                    if (!repo.exists(it)) {
                        throw IllegalArgumentException("Parent account with ID $it does not exist.")
                    }
                }

                val newAccount = Account(
                    id = Account.Id.random(),
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

    suspend fun renameAccount(command: RenameAccountCommand) {
        require(command.newName.isNotBlank()) { "New account name must not be blank." }

        uowFactory.create().transaction { uow ->
            val repo = uow.chartOfAccountsRepository()
            val account = repo.findById(command.accountId)
                ?: throw IllegalArgumentException("Account with ID ${command.accountId} not found.")

            val updatedAccount = account.copy(name = command.newName)
            repo.update(updatedAccount)
        }
    }
}