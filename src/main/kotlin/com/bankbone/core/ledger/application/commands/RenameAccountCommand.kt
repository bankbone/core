package com.bankbone.core.ledger.application.commands

import com.bankbone.core.ledger.domain.Account

data class RenameAccountCommand(
    val accountId: Account.Id,
    val newName: String
)