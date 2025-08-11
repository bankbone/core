package com.bankbone.core.ledger.infrastructure.dto

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.domain.LedgerTransactionId
import com.bankbone.core.ledger.domain.LedgerEntry
import com.bankbone.core.ledger.infrastructure.dto.LedgerEntryDto
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * Data Transfer Object for LedgerTransaction.
 * Used for serialization/deserialization of LedgerTransaction to/from external representations.
 * 
 * @property id The unique identifier of the transaction
 * @property sourceTransactionId The source transaction ID from the external system
 * @property description Human-readable description of the transaction
 * @property entries List of ledger entries that make up this transaction
 * @property postedAt ISO-8601 timestamp when the transaction was posted
 */
@Serializable
data class LedgerTransactionDto(
    val id: LedgerTransactionId,
    val sourceTransactionId: String,
    val description: String,
    val entries: List<LedgerEntryDto>,
    val postedAt: String
) {
    companion object {
        /**
         * Creates a DTO from a domain LedgerTransaction.
         */
        fun fromDomain(transaction: LedgerTransaction): LedgerTransactionDto = LedgerTransactionDto(
            id = transaction.id,
            sourceTransactionId = transaction.sourceTransactionId,
            description = transaction.description,
            entries = transaction.entries.map { LedgerEntryDto.fromDomain(it) },
            postedAt = transaction.postedAt.toString()
        )
        
        /**
         * Converts this DTO to a domain LedgerTransaction.
         */
        fun toDomain(dto: LedgerTransactionDto): LedgerTransaction = LedgerTransaction(
            id = dto.id,
            sourceTransactionId = dto.sourceTransactionId,
            description = dto.description,
            entries = dto.entries.map { LedgerEntryDto.toDomain(it) },
            postedAt = Instant.parse(dto.postedAt)
        )
    }
}
