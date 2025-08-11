package com.bankbone.core.ledger.domain

import com.bankbone.core.sharedkernel.domain.AggregateRoot
import com.bankbone.core.sharedkernel.domain.ids.TypedId
import com.bankbone.core.sharedkernel.domain.Asset
import kotlinx.serialization.Serializable

/**
 * Type-safe identifier for Account entities.
 * This provides type safety when working with Account IDs throughout the application.
 */
typealias AccountId = TypedId<Account>

/**
 * Represents the type of an account in the ledger system.
 */
@Serializable
enum class AccountType { 
    /** Asset accounts represent resources owned by the entity */
    ASSET, 
    
    /** Liability accounts represent obligations or debts */
    LIABILITY, 
    
    /** Equity accounts represent ownership interest */
    EQUITY, 
    
    /** Revenue accounts track income */
    REVENUE, 
    
    /** Expense accounts track costs */
    EXPENSE 
}

/**
 * Represents an account in the ledger system.
 * 
 * @property id Unique identifier for the account
 * @property name Human-readable name of the account
 * @property type The type of account (e.g., ASSET, LIABILITY, etc.)
 * @property asset The type of asset this account tracks (e.g., "BRL", "BTC")
 * @property parentAccountId Optional parent account ID for hierarchical accounts
 * @property isActive Whether the account is currently active
 * @property metadata Additional key-value pairs for extensibility
 */
@Serializable
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
data class Account(
    override val id: AccountId,
    val name: String,
    val type: AccountType,
    val asset: Asset,
    val parentAccountId: AccountId? = null,
    val isActive: Boolean = true,
    val metadata: Map<String, String> = emptyMap()
) : AggregateRoot<AccountId>() {
    
    init {
        require(name.isNotBlank()) { "Account name must not be blank" }
    }
    /**
     * Creates a copy of the account with the specified changes.
     *
     * @param name New name for the account (defaults to current name)
     * @param isActive New active status for the account (defaults to current status)
     * @param metadata New metadata for the account (defaults to current metadata)
     * @return A new Account instance with the specified changes
     */
    fun copyWith(
        name: String = this.name,
        isActive: Boolean = this.isActive,
        metadata: Map<String, String> = this.metadata
    ): Account = copy(
        name = name,
        isActive = isActive,
        metadata = metadata.toMutableMap()
    )

    companion object {
        /**
         * Factory method to create a new Account.
         * Validates the account and ensures required fields are provided.
         */
        fun create(
            name: String,
            type: AccountType,
            asset: Asset,
            parentAccountId: AccountId? = null,
            isActive: Boolean = true,
            metadata: Map<String, String> = emptyMap()
        ): Account {
            return Account(
                id = TypedId.random(),
                name = name,
                type = type,
                asset = asset,
                parentAccountId = parentAccountId,
                isActive = isActive,
                metadata = metadata
            )
        }
    }
}
