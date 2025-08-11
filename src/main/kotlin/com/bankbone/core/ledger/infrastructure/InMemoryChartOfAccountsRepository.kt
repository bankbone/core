package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountId
import com.bankbone.core.ledger.ports.ChartOfAccountsRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryChartOfAccountsRepository : ChartOfAccountsRepository {
    private val accounts = ConcurrentHashMap<AccountId, Account>()

    override suspend fun add(account: Account) {
        accounts[account.id] = account
    }

    override suspend fun update(account: Account) {
        accounts[account.id] = account
    }

    override suspend fun findById(accountId: AccountId): Account? = accounts[accountId]
    override suspend fun exists(accountId: AccountId): Boolean = accounts.containsKey(accountId)
    override suspend fun listAll(): List<Account> = accounts.values.toList()
    override suspend fun findByAsset(asset: String): List<Account> {
        return accounts.values.filter { it.asset.code == asset }
    }
    override suspend fun findAllByIds(accountIds: Collection<AccountId>): List<Account> {
        return accounts.values.filter { it.id in accountIds }
    }

    fun clear() = accounts.clear()
}