package com.bankbone.core.ledger.domain

import com.bankbone.core.sharedkernel.domain.ids.TypedId
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.Amount
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Type-safe identifier for LedgerEntry entities.
 * This provides type safety when working with LedgerEntry IDs throughout the application.
 */
typealias LedgerEntryId = TypedId<LedgerEntry>

/**
 * Represents the type of a ledger entry (debit or credit).
 * In double-entry bookkeeping, every transaction affects at least two accounts:
 * one account is debited and another is credited.
 */
@Serializable
enum class LedgerEntryType { 
    /** Represents an entry that increases asset/expense accounts or decreases liability/equity/revenue accounts */
    DEBIT, 
    
    /** Represents an entry that decreases asset/expense accounts or increases liability/equity/revenue accounts */
    CREDIT 
}

/**
 * Represents a single entry in a ledger transaction.
 * Each entry is associated with one account and indicates whether it's a debit or credit.
 *
 * @property accountId The ID of the account this entry affects
 * @property amount The monetary amount of the entry
 * @property type Whether this is a DEBIT or CREDIT entry
 * @property description Optional description of the entry
 */
@Serializable
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
data class LedgerEntry(
    val accountId: AccountId,
    val amount: Amount,
    val type: LedgerEntryType,
    val description: String? = null
) {
    /**
     * The asset type of this entry's amount.
     * Derived from the amount's asset property.
     */
    val asset: Asset
        get() = amount.asset

    init {
        require(amount.value > BigDecimal.ZERO) { "Amount must be positive" }
    }
}
