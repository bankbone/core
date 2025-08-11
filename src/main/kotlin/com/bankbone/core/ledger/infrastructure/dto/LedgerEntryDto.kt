package com.bankbone.core.ledger.infrastructure.dto

import com.bankbone.core.ledger.domain.AccountId
import com.bankbone.core.ledger.domain.LedgerEntry
import com.bankbone.core.ledger.domain.LedgerEntryType
import com.bankbone.core.sharedkernel.domain.Amount
import com.bankbone.core.sharedkernel.domain.Asset
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Data Transfer Object for LedgerEntry.
 * Used for serialization/deserialization of LedgerEntry to/from external representations.
 * 
 * @property accountId The ID of the account this entry affects
 * @property amount The amount of the entry as a string (e.g., "100.00")
 * @property asset The code of the asset (e.g., "USD", "BTC")
 * @property type The type of entry ("DEBIT" or "CREDIT")
 * @property description Optional description of the entry
 */
@Serializable
data class LedgerEntryDto(
    /**
     * The ID of the account this entry affects
     */
    val accountId: AccountId,
    val amount: String,
    val asset: String,
    val type: String,
    val description: String? = null
) {
    companion object {
        /**
         * Creates a DTO from a domain LedgerEntry.
         */
        fun fromDomain(entry: LedgerEntry): LedgerEntryDto = LedgerEntryDto(
            accountId = entry.accountId,
            amount = entry.amount.value.toPlainString(),
            asset = entry.asset.code,
            type = entry.type.name,
            description = entry.description
        )
        
        /**
         * Converts this DTO to a domain LedgerEntry.
         */
        fun toDomain(dto: LedgerEntryDto): LedgerEntry = LedgerEntry(
            accountId = dto.accountId,
            amount = Amount(BigDecimal(dto.amount), Asset(dto.asset)),
            type = LedgerEntryType.valueOf(dto.type),
            description = dto.description
        )
    }
}
