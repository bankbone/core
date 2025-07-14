package com.bankbone.core.ledger.domain

import com.bankbone.core.sharedkernel.domain.Asset
import java.util.UUID

enum class AccountType { ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE }

data class Account(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AccountType,
    val asset: Asset, // Represents the asset type (e.g., "BRL", "Gold", "BTC")
    val parentAccountId: String? = null, // Supports hierarchical structure
    val isActive: Boolean = true, // Indicates if the account is active
    val metadata: Map<String, String> = emptyMap() // Additional attributes for extensibility
) {
    init {
        require(name.isNotBlank()) { "Account name must not be blank" }
    }
}
