package com.bankbone.core.ledger.infrastructure

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.Account.Id
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.ledger.ports.ChartOfAccountsRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryChartOfAccountsRepository : ChartOfAccountsRepository {
    private val accounts = ConcurrentHashMap<Id, Account>()

    override suspend fun exists(accountId: Id): Boolean {
        return accounts.containsKey(accountId) && accounts[accountId]?.isActive == true
    }

    override suspend fun findById(accountId: Id): Account? {
        return accounts[accountId]
    }

    override suspend fun listAll(): List<Account> {
        return accounts.values.toList()
    }

    override suspend fun add(account: Account) {
        require(!accounts.containsKey(account.id)) { "Account with ID ${account.id} already exists." }
        accounts[account.id] = account
    }

    override suspend fun update(account: Account) {
        require(accounts.containsKey(account.id)) { "Cannot update account with ID ${account.id} because it does not exist." }
        accounts[account.id] = account
    }

    override suspend fun findByAsset(asset: String): List<Account> {
        val assetVO = Asset(asset)
        return accounts.values.filter { it.asset == assetVO }
    }

    override suspend fun findAllByIds(accountIds: Collection<Id>): List<Account> {
        return accountIds.mapNotNull { accounts[it] }.filter { it.isActive }
    }

    fun clear() {
        accounts.clear()
    }
}
