package com.bankbone.core.ledger.infrastructure.mappers

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountId
import com.bankbone.core.ledger.domain.AccountType
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.ledger.infrastructure.dto.AccountDto

object AccountMappers {
    fun toDto(account: Account): AccountDto = AccountDto(
        id = account.id,
        name = account.name,
        type = account.type.name,
        asset = account.asset.code,
        parentAccountId = account.parentAccountId,
        isActive = account.isActive,
        metadata = account.metadata
    )
    
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
