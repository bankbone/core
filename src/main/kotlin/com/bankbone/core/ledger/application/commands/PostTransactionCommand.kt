package com.bankbone.core.ledger.application.commands

import com.bankbone.core.ledger.domain.LedgerEntry
import com.bankbone.core.ledger.domain.LedgerEntryType
import com.bankbone.core.sharedkernel.domain.IdempotencyKey

data class PostTransactionCommand(
    val sourceTransactionId: String,
    val description: String,
    val entries: List<LedgerEntry>
) {
    var idempotencyKey: IdempotencyKey = IdempotencyKey()
}
