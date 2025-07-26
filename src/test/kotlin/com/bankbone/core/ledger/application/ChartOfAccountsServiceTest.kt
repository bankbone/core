package com.bankbone.core.ledger.application

import com.bankbone.core.ledger.application.commands.CreateAccountCommand
import com.bankbone.core.ledger.application.commands.RenameAccountCommand
import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountType
import com.bankbone.core.ledger.infrastructure.InMemoryChartOfAccountsRepository
import com.bankbone.core.ledger.infrastructure.InMemoryLedgerUnitOfWorkFactory
import com.bankbone.core.sharedkernel.di.applicationModule
import com.bankbone.core.sharedkernel.di.sharedKernelModule
import com.bankbone.core.sharedkernel.di.testingPersistenceModule
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.IdempotencyKey
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ChartOfAccountsServiceTest : KoinTest {

    private val chartOfAccountsService: ChartOfAccountsService by inject()
    private val uowFactory: InMemoryLedgerUnitOfWorkFactory by inject()
    private lateinit var chartOfAccountsRepository: InMemoryChartOfAccountsRepository

    private val brl = Asset("BRL")

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(sharedKernelModule, applicationModule, testingPersistenceModule)
    }

    @BeforeEach
    fun setUp() {
        // Since the factory holds the state for our in-memory tests, we can access
        // the repository through it and clear it for test isolation.
        chartOfAccountsRepository = uowFactory.chartOfAccountsRepository
        chartOfAccountsRepository.clear()
    }

    @Test
    fun `should create an account successfully`() = runBlocking {
        val command = CreateAccountCommand("Cash", AccountType.ASSET, brl)
        val account = chartOfAccountsService.createAccount(command)

        assertNotNull(account)
        assertEquals("Cash", account.name)

        val savedAccount = chartOfAccountsRepository.findById(account.id)
        assertNotNull(savedAccount)
        assertEquals(account.id, savedAccount.id)
    }

    @Test
    fun `should create a child account successfully`() = runBlocking {
        val parentCommand = CreateAccountCommand("Parent Asset", AccountType.ASSET, brl)
        val parentAccount = chartOfAccountsService.createAccount(parentCommand)

        val childCommand = CreateAccountCommand("Child Asset", AccountType.ASSET, brl, parentAccountId = parentAccount.id.toString())
        val childAccount = chartOfAccountsService.createAccount(childCommand)

        assertEquals(parentAccount.id, childAccount.parentAccountId)
    }

    @Test
    fun `should fail to create account with non-existent parent`() = runBlocking {
        val nonExistentParentId = Account.Id.random().toString()
        val command = CreateAccountCommand("Child Asset", AccountType.ASSET, brl, parentAccountId = nonExistentParentId)

        val exception = assertFailsWith<IllegalArgumentException> {
            chartOfAccountsService.createAccount(command)
        }
        assertEquals("Parent account with ID $nonExistentParentId does not exist.", exception.message)
    }

    @Test
    fun `should not create duplicate account for same idempotency key`() = runBlocking {
        val key = IdempotencyKey()
        val command = CreateAccountCommand("Cash", AccountType.ASSET, brl, idempotencyKey = key)

        val account1 = chartOfAccountsService.createAccount(command)
        val account2 = chartOfAccountsService.createAccount(command)

        assertEquals(account1.id, account2.id)
        assertEquals(1, chartOfAccountsRepository.listAll().size)
    }

    @Test
    fun `should rename an account successfully`() = runBlocking {
        val createCommand = CreateAccountCommand("Old Name", AccountType.LIABILITY, brl)
        val account = chartOfAccountsService.createAccount(createCommand)

        val renameCommand = RenameAccountCommand(account.id, "New Name")
        chartOfAccountsService.renameAccount(renameCommand)

        val updatedAccount = chartOfAccountsRepository.findById(account.id)
        assertNotNull(updatedAccount)
        assertEquals("New Name", updatedAccount.name)
    }

    @Test
    fun `should fail to rename a non-existent account`() = runBlocking {
        val nonExistentId = Account.Id.random()
        val command = RenameAccountCommand(nonExistentId, "New Name")

        val exception = assertFailsWith<IllegalArgumentException> {
            chartOfAccountsService.renameAccount(command)
        }
        assertEquals("Account with ID $nonExistentId not found.", exception.message)
    }
}