package com.bankbone.core.ledger.infrastructure.dto

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountId
import com.bankbone.core.ledger.domain.AccountType
import com.bankbone.core.sharedkernel.domain.Asset
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Data Transfer Object for Account entities.
 * Used for serialization/deserialization of Account data across boundaries.
 */
@Serializable
data class AccountDto(
    val id: AccountId,
    val name: String,
    val type: String,
    val asset: String,
    val parentAccountId: AccountId? = null,
    val isActive: Boolean = true,
    val metadata: Map<String, String> = emptyMap()
)
