package com.bankbone.core.ledger.application.usecases

import com.bankbone.core.ledger.application.commands.CreateAccountCommand
import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountType
import com.bankbone.core.ledger.infrastructure.InMemoryChartOfAccountsRepository
import com.bankbone.core.ledger.infrastructure.InMemoryLedgerUnitOfWorkFactory
import com.bankbone.core.sharedkernel.application.CommandHandler
import com.bankbone.core.sharedkernel.di.applicationModule
import com.bankbone.core.sharedkernel.di.sharedKernelModule
import com.bankbone.core.sharedkernel.di.testingPersistenceModule
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.IdempotencyKey
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CreateAccountUseCaseTest : KoinTest, KoinComponent {

    private val createAccountHandler: CommandHandler<CreateAccountCommand, Account> by inject(named("CreateAccountCommandHandler"))
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
        chartOfAccountsRepository = uowFactory.chartOfAccountsRepository
        chartOfAccountsRepository.clear()
    }

    @Test
    fun `should create an account successfully`() = runBlocking {
        val command = CreateAccountCommand("Cash", AccountType.ASSET, brl)
        val account = createAccountHandler.handle(command)

        assertNotNull(account)
        assertEquals("Cash", account.name)

        val savedAccount = chartOfAccountsRepository.findById(account.id)
        assertNotNull(savedAccount)
        assertEquals(account.id, savedAccount.id)
    }

    @Test
    fun `should create a child account successfully`() = runBlocking {
        val parentCommand = CreateAccountCommand(
            name = "Parent Asset",
            type = AccountType.ASSET,
            asset = brl
        )
        val parentAccount = createAccountHandler.handle(parentCommand)

        val childCommand = CreateAccountCommand(
            name = "Child Asset",
            type = AccountType.ASSET,
            asset = brl,
            parentAccountId = parentAccount.id.value.toString()
        )
        val childAccount = createAccountHandler.handle(childCommand)

        assertEquals(parentAccount.id, childAccount.parentAccountId)
    }

    @Test
    fun `should fail to create account with non-existent parent`() = runBlocking {
        val nonExistentParentId = Account.Id.random()
        val command = CreateAccountCommand(
            name = "Child Asset", 
            type = AccountType.ASSET, 
            asset = brl, 
            parentAccountId = nonExistentParentId.value.toString()
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            createAccountHandler.handle(command)
        }
        assertEquals("Parent account with ID ${nonExistentParentId.value} does not exist.", exception.message)
    }

    @Test
    fun `should not create duplicate account for same idempotency key`() = runBlocking {
        val key = IdempotencyKey()
        val command = CreateAccountCommand("Cash", AccountType.ASSET, brl, idempotencyKey = key)

        val account1 = createAccountHandler.handle(command)
        val account2 = createAccountHandler.handle(command)

        assertEquals(account1.id, account2.id)
        assertEquals(1, chartOfAccountsRepository.listAll().size)
    }
}