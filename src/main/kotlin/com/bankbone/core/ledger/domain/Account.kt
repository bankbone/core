package com.bankbone.core.ledger.domain

import com.bankbone.core.ledger.domain.serializers.AccountIdSerializer
import com.bankbone.core.sharedkernel.domain.AggregateId
import com.bankbone.core.sharedkernel.domain.Asset
import kotlinx.serialization.Serializable
import java.util.UUID

enum class AccountType { ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE }

data class Account(
    val id: Id,
    val name: String,
    val type: AccountType,
    val asset: Asset, // Represents the asset type (e.g., "BRL", "Gold", "BTC")
    val parentAccountId: Id? = null, // Supports hierarchical structure
    val isActive: Boolean = true, // Indicates if the account is active
    val metadata: Map<String, String> = emptyMap() // Additional attributes for extensibility
) {
    init {
        require(name.isNotBlank()) { "Account name must not be blank" }
    }

    @Serializable(with = AccountIdSerializer::class)
    data class Id(override val value: UUID) : AggregateId(value) {
        companion object {
            fun random(): Id = Id(UUID.randomUUID())
            fun fromString(id: String): Id = Id(UUID.fromString(id))
        }

        override fun toString(): String = value.toString()
    }
}
