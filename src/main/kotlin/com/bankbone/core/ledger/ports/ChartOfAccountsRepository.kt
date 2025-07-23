package com.bankbone.core.ledger.ports

import com.bankbone.core.ledger.domain.Account

interface ChartOfAccountsRepository {
    suspend fun exists(accountId: Account.Id): Boolean
    suspend fun findById(accountId: Account.Id): Account?
    suspend fun listAll(): List<Account>
    suspend fun add(account: Account)
    suspend fun findByAsset(asset: String): List<Account> // New method to find accounts by asset type
    suspend fun findAllByIds(accountIds: Collection<Account.Id>): List<Account>
}
