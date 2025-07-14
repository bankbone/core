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

    /**
     * The total value of the transaction, calculated as the sum of all debit entries.
     * This is a computed property, cached with `lazy` for efficiency. It assumes the transaction is balanced.
     */
    val totalAmount: BigDecimal by lazy {
        entries.filter { it.type == LedgerEntryType.DEBIT }
            .fold(BigDecimal.ZERO) { acc, entry -> acc + entry.amount.value }
    }

    init {
        validateState()
        recordEvent(LedgerTransactionPosted(id, totalAmount, entries.first().asset, postedAt))
    }

    fun validate() {
        validateState()
    }

    private fun validateState() {
        validateHasEnoughEntries()
        validateSameAsset()
        validateBalance()
    }

    private fun validateHasEnoughEntries() {
        require(entries.size >= 2) { "A balanced ledger transaction must have at least two entries." }
    }

    private fun validateSameAsset() {
        val firstAsset = entries.first().asset
        require(entries.all { it.asset == firstAsset }) { "All entries in a transaction must have the same asset. Found mixed assets." }
    }

    private fun validateBalance() {
        val totalCredit = entries.filter { it.type == LedgerEntryType.CREDIT }.fold(BigDecimal.ZERO) { acc, entry -> acc + entry.amount.value }
        require(totalAmount == totalCredit) { "Ledger transaction is unbalanced. Debits ($totalAmount) do not equal credits ($totalCredit) for asset ${entries.first().asset}." }
    }
}
