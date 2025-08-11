package com.bankbone.core.ledger.domain.events

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.domain.LedgerTransactionId
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.DomainEvent
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.jetbrains.annotations.VisibleForTesting
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Domain event that is raised when a new ledger transaction is posted.
 * This event contains all the necessary information about the transaction
 * that might be needed by other parts of the system.
 */
@Serializable
class LedgerTransactionPosted private constructor(
    @Contextual
    val transactionId: LedgerTransactionId,
    @Contextual
    val totalAmount: @Contextual BigDecimal,
    val asset: Asset,
    @Contextual
    override val occurredAt: Instant
) : DomainEvent {
    
    // Override properties from DomainEvent interface
    @Contextual
    override val aggregateId: UUID = transactionId.value
    
    override val eventType: String = "LedgerTransactionPosted"

    companion object {
        /**
         * Factory method that creates a LedgerTransactionPosted event from a LedgerTransaction.
         * 
         * @param transaction The transaction that was posted
         * @return A new LedgerTransactionPosted event
         */
        fun from(transaction: LedgerTransaction): LedgerTransactionPosted {
            require(transaction.entries.isNotEmpty()) { "Cannot create event from transaction with no entries" }
            
            return LedgerTransactionPosted(
                transactionId = transaction.id,
                totalAmount = transaction.entries
                    .filter { it.type == com.bankbone.core.ledger.domain.LedgerEntryType.DEBIT }
                    .sumOf { it.amount.value },
                asset = transaction.entries.first().asset,
                occurredAt = transaction.postedAt
            )
        }
        
        /**
         * Test helper method for creating LedgerTransactionPosted instances in tests.
         * This should only be used in test code.
         * 
         * @param transactionId The ID of the transaction
         * @param totalAmount The total amount of the transaction
         * @param asset The asset of the transaction
         * @param occurredAt When the event occurred (defaults to now)
         * @return A new LedgerTransactionPosted instance
         */
        @VisibleForTesting
        fun createForTest(
            transactionId: LedgerTransactionId,
            totalAmount: BigDecimal,
            asset: Asset,
            occurredAt: Instant = Instant.now()
        ): LedgerTransactionPosted {
            require(totalAmount > BigDecimal.ZERO) { "Total amount must be positive" }
            
            return LedgerTransactionPosted(
                transactionId = transactionId,
                totalAmount = totalAmount,
                asset = asset,
                occurredAt = occurredAt
            )
        }
        
        /**
         * Serializer for the LedgerTransactionPosted class.
         * Used for serialization/deserialization of the event.
         */
        val serializer = serializer<LedgerTransactionPosted>()
    }
}
