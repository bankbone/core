package com.bankbone.core.ledger.domain

import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.sharedkernel.domain.AggregateId
import com.bankbone.core.sharedkernel.domain.AggregateRoot
import com.bankbone.core.sharedkernel.domain.Amount
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class LedgerTransaction(
    override val id: Id,
    val sourceTransactionId: String,
    val description: String,
    val entries: List<LedgerEntry>,
    val postedAt: Instant = Instant.now()
) : AggregateRoot<LedgerTransaction.Id>() {

    data class Id(override val value: UUID) : AggregateId(value) {
        companion object {
            fun random(): Id = Id(UUID.randomUUID())
            fun fromString(string: String): Id = Id(UUID.fromString(string))
        }
    }

    init {
        require(entries.size >= 2) { "A balanced ledger transaction must have at least two entries." }
        
        val firstAsset = entries.firstOrNull()?.asset
        require(!firstAsset?.code.isNullOrBlank()) { "Transaction entries must have a valid asset." }
        require(entries.all { it.asset == firstAsset }) { "All entries in a transaction must have the same asset. Found mixed assets." }

        val debits = entries.filter { it.type == LedgerEntryType.DEBIT }.sumOf { it.amount.value }
        val credits = entries.filter { it.type == LedgerEntryType.CREDIT }.sumOf { it.amount.value }
        require(debits == credits) { "Ledger transaction is unbalanced. Debits ($debits) do not equal credits ($credits)." }
        require(debits > BigDecimal.ZERO) { "Transaction amount must be positive." }
    }

    companion object {
        fun create(sourceTransactionId: String, description: String, entries: List<LedgerEntry>): LedgerTransaction {
            val transaction = LedgerTransaction(
                id = Id.random(),
                sourceTransactionId = sourceTransactionId,
                description = description,
                entries = entries
            )
            transaction.addDomainEvent(LedgerTransactionPosted.from(transaction))
            return transaction
        }
    }
}