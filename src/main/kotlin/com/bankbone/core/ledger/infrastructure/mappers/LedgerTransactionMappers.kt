package com.bankbone.core.ledger.infrastructure.mappers

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountId
import com.bankbone.core.ledger.domain.LedgerEntry
import com.bankbone.core.ledger.domain.LedgerEntryType
import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.domain.LedgerTransactionId
import com.bankbone.core.ledger.infrastructure.dto.LedgerEntryDto
import com.bankbone.core.ledger.infrastructure.dto.LedgerTransactionDto
import com.bankbone.core.sharedkernel.domain.Amount
import com.bankbone.core.sharedkernel.domain.Asset
import java.time.Instant

/**
 * Mappers for converting between LedgerTransaction domain models and DTOs.
 * Handles the conversion between domain models and their DTO representations.
 */
object LedgerTransactionMappers {
    /**
     * Converts a domain LedgerTransaction to its DTO representation.
     * @param transaction The domain model to convert
     * @return The DTO representation of the transaction
     */
    fun toDto(transaction: LedgerTransaction): LedgerTransactionDto = LedgerTransactionDto(
        id = transaction.id,
        sourceTransactionId = transaction.sourceTransactionId,
        description = transaction.description,
        entries = transaction.entries.map { toDto(it) },
        postedAt = transaction.postedAt.toString()
    )
    
    /**
     * Converts a LedgerTransaction DTO back to its domain representation.
     * @param dto The DTO to convert
     * @return The domain model representation of the transaction
     */
    fun toDomain(dto: LedgerTransactionDto): LedgerTransaction = LedgerTransaction(
        id = dto.id,
        sourceTransactionId = dto.sourceTransactionId,
        description = dto.description,
        entries = dto.entries.map { toDomain(it) },
        postedAt = Instant.parse(dto.postedAt)
    )
    
    private fun toDto(entry: LedgerEntry): LedgerEntryDto = LedgerEntryDto(
        accountId = entry.accountId,
        amount = entry.amount.value.toString(),
        asset = entry.asset.code,
        type = entry.type.name,
        description = entry.description
    )
    
    private fun toDomain(dto: LedgerEntryDto): LedgerEntry = LedgerEntry(
        accountId = dto.accountId,
        amount = Amount(dto.amount.toBigDecimal(), Asset(dto.asset)),
        type = LedgerEntryType.valueOf(dto.type),
        description = dto.description
    )
}
