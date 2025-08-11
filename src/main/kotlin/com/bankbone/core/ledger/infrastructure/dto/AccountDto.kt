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
) {
    companion object {
        /**
         * Creates a DTO from a domain Account entity.
         */
        fun fromDomain(account: Account): AccountDto = AccountDto(
            id = account.id,
            name = account.name,
            type = account.type.name,
            asset = account.asset.code,
            parentAccountId = account.parentAccountId,
            isActive = account.isActive,
            metadata = account.metadata
        )
        
        /**
         * Converts this DTO to a domain Account.
         */
        fun toDomain(dto: AccountDto): Account = Account(
            id = dto.id,
            name = dto.name,
            type = AccountType.valueOf(dto.type),
            asset = Asset(dto.asset),
            parentAccountId = dto.parentAccountId,
            isActive = dto.isActive,
            metadata = dto.metadata
        )
    }
}
