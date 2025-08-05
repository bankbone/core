package com.bankbone.core.ledger.application.usecases

import com.bankbone.core.ledger.application.commands.CreateAccountCommand
import com.bankbone.core.ledger.application.commands.RenameAccountCommand
import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountType
import com.bankbone.core.ledger.infrastructure.InMemoryChartOfAccountsRepository
import com.bankbone.core.ledger.infrastructure.InMemoryLedgerUnitOfWorkFactory
import com.bankbone.core.sharedkernel.application.CommandHandler
import com.bankbone.core.sharedkernel.di.applicationModule
import com.bankbone.core.sharedkernel.di.sharedKernelModule
import com.bankbone.core.sharedkernel.di.testingPersistenceModule
import com.bankbone.core.sharedkernel.domain.Asset
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

class RenameAccountUseCaseTest : KoinTest {

    private val createAccountHandler: CommandHandler<CreateAccountCommand, Account> by inject()
    private val renameAccountHandler: CommandHandler<RenameAccountCommand, Unit> by inject()
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
    fun `should rename an account successfully`() = runBlocking {
        // Create an account directly through the repository for testing purposes
        val account = Account(
            id = Account.Id.random(),
            name = "Old Name",
            type = AccountType.LIABILITY,
            asset = brl
        )
        
        // Save the account directly to the repository for testing
        chartOfAccountsRepository.add(account)

        // Now test the rename functionality
        val renameCommand = RenameAccountCommand(account.id, "New Name")
        renameAccountHandler.handle(renameCommand)

        val updatedAccount = chartOfAccountsRepository.findById(account.id)
        assertNotNull(updatedAccount)
        assertEquals("New Name", updatedAccount.name)
    }

    @Test
    fun `should fail to rename a non-existent account`() = runBlocking {
        val nonExistentId = Account.Id.random()
        val command = RenameAccountCommand(nonExistentId, "New Name")

        val exception = assertFailsWith<IllegalArgumentException> {
            renameAccountHandler.handle(command)
        }
        assertEquals("Account with ID $nonExistentId not found.", exception.message)
    }
}