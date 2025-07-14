package com.bankbone.core.ledger.application.commands

import com.bankbone.core.ledger.domain.LedgerEntry

data class PostTransactionCommand(
    val sourceTransactionId: String,
    val description: String,
    val entries: List<LedgerEntry>
)