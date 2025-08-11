package com.bankbone.core.ledger.domain

import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.sharedkernel.domain.AggregateRoot
import com.bankbone.core.sharedkernel.domain.ids.TypedId
import com.bankbone.core.sharedkernel.domain.Amount
import com.bankbone.core.sharedkernel.infrastructure.serialization.InstantAsStringSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.Instant

/**
 * Type-safe identifier for LedgerTransaction entities.
 * This provides type safety when working with LedgerTransaction IDs throughout the application.
 */
typealias LedgerTransactionId = TypedId<LedgerTransaction>

/**
 * Represents a transaction in the ledger, ensuring double-entry bookkeeping rules.
 * Each transaction must have at least two entries (one debit and one credit) that balance.
 */
@Serializable
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
data class LedgerTransaction(
    override val id: LedgerTransactionId,
    val sourceTransactionId: String,
    val description: String,
    val entries: List<LedgerEntry>,
    @Serializable(with = InstantAsStringSerializer::class)
    val postedAt: Instant = Instant.now()
) : AggregateRoot<LedgerTransactionId>() {
    init {
        require(entries.size >= 2) { "A balanced ledger transaction must have at least two entries." }
        
        val firstAsset = entries.firstOrNull()?.asset
        require(!firstAsset?.code.isNullOrBlank()) { "Transaction entries must have a valid asset." }
        require(entries.all { it.asset == firstAsset }) { 
            "All entries in a transaction must have the same asset. Found mixed assets." 
        }

        val debits = entries.filter { it.type == LedgerEntryType.DEBIT }.sumOf { it.amount.value }
        val credits = entries.filter { it.type == LedgerEntryType.CREDIT }.sumOf { it.amount.value }
        require(debits == credits) { 
            "Ledger transaction is unbalanced. Debits ($debits) do not equal credits ($credits)." 
        }
        require(debits > BigDecimal.ZERO) { "Transaction amount must be positive." }
    }

    companion object {
        /**
         * Factory method to create a new LedgerTransaction.
         * Validates the transaction and raises appropriate domain events.
         */
        fun create(sourceTransactionId: String, description: String, entries: List<LedgerEntry>): LedgerTransaction {
            val transaction = LedgerTransaction(
                id = TypedId.random(),
                sourceTransactionId = sourceTransactionId,
                description = description,
                entries = entries
            )
            transaction.addDomainEvent(LedgerTransactionPosted.from(transaction))
            return transaction
        }
    }
}
