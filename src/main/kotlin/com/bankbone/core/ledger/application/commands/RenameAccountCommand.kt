package com.bankbone.core.ledger.application.commands

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.sharedkernel.application.ShardedCommand

data class RenameAccountCommand(
    val accountId: Account.Id,
    val newName: String
) : ShardedCommand {
    override val shardKey: String
        get() = accountId.toString()
}