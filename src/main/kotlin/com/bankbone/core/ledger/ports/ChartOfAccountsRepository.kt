package com.bankbone.core.ledger.ports

import com.bankbone.core.ledger.domain.Account

interface ChartOfAccountsRepository {
    suspend fun exists(accountId: String): Boolean
    suspend fun findById(accountId: String): Account?
    suspend fun listAll(): List<Account>
    suspend fun add(account: Account)
    suspend fun findByAsset(asset: String): List<Account> // New method to find accounts by asset type
    suspend fun findAllByIds(accountIds: Collection<String>): List<Account>
}
