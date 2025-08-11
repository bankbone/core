package com.bankbone.core.ledger.ports

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountId

interface ChartOfAccountsRepository {
    suspend fun exists(accountId: AccountId): Boolean
    suspend fun findById(accountId: AccountId): Account?
    suspend fun listAll(): List<Account>
    suspend fun add(account: Account)
    suspend fun findByAsset(asset: String): List<Account>
    suspend fun update(account: Account)
    suspend fun findAllByIds(accountIds: Collection<AccountId>): List<Account>
}
