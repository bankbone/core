package com.bankbone.core.ledger.application.commands

import com.bankbone.core.ledger.domain.AccountId
import com.bankbone.core.sharedkernel.application.ShardedCommand

/**
 * Command to rename an existing account.
 *
 * @property accountId The ID of the account to rename
 * @property newName The new name for the account
 */
data class RenameAccountCommand(
    val accountId: AccountId,
    val newName: String
) : ShardedCommand {
    override val shardKey: String
        get() = accountId.toString()
}