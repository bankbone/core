package com.bankbone.core.ledger.application.commands

import com.bankbone.core.ledger.domain.AccountType
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.IdempotencyKey

data class CreateAccountCommand(
    val name: String,
    val type: AccountType,
    val asset: Asset,
    val parentAccountId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    var idempotencyKey: IdempotencyKey = IdempotencyKey()
)