package com.bankbone.core.ledger.domain

import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.sharedkernel.domain.AggregateRoot
import java.math.BigDecimal
import java.time.Instant

data class LedgerTransaction(
    val id: String,
    val sourceTransactionId: String,
    val description: String,
    val entries: List<LedgerEntry>,
    val postedAt: Instant = Instant.now()
) : AggregateRoot() {
    init {
        val totalAmount = validateStateAndGetTotal()
        recordEvent(LedgerTransactionPosted(id, totalAmount, entries.first().asset, postedAt))
    }

    fun validate() {
        validateStateAndGetTotal()
    }

    private fun validateStateAndGetTotal(): BigDecimal {
        // Invariant: Must have at least two entries for a balanced transaction.
        require(entries.size >= 2) { "A balanced ledger transaction must have at least two entries." }

        // Invariant: All entries must have the same asset.
        val firstAsset = entries.first().asset
        require(entries.all { it.asset == firstAsset }) { "All entries in a transaction must have the same asset. Found mixed assets." }

        // Invariant: Debits must equal credits.
        val totalDebit = entries.filter { it.type == LedgerEntryType.DEBIT }.fold(BigDecimal.ZERO) { acc, entry -> acc + entry.amount.value }
        val totalCredit = entries.filter { it.type == LedgerEntryType.CREDIT }.fold(BigDecimal.ZERO) { acc, entry -> acc + entry.amount.value }
        require(totalDebit == totalCredit) { "Ledger transaction is unbalanced. Debits ($totalDebit) do not equal credits ($totalCredit) for asset $firstAsset." }

        return totalDebit
    }
}
