package com.bankbone.core.ledger.application

import com.bankbone.core.ledger.application.commands.PostTransactionCommand
import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountType
import com.bankbone.core.ledger.domain.LedgerEntry
import com.bankbone.core.ledger.domain.LedgerEntryType
import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.ledger.infrastructure.InMemoryLedgerUnitOfWorkFactory
import com.bankbone.core.sharedkernel.infrastructure.serialization.KotlinxEventDeserializer
import com.bankbone.core.sharedkernel.di.applicationModule
import com.bankbone.core.sharedkernel.di.sharedKernelModule
import com.bankbone.core.sharedkernel.di.testingPersistenceModule
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.Amount
import com.bankbone.core.sharedkernel.domain.IdempotencyKey
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.*
import java.math.BigDecimal
class LedgerPostingServiceTest : KoinTest {

    // Inject dependencies from the Koin container configured for testing
    private val ledgerPostingService: LedgerPostingService by inject()
    private val uowFactory: InMemoryLedgerUnitOfWorkFactory by inject() // Injected for test inspection

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(sharedKernelModule, applicationModule, testingPersistenceModule)
    }

    @BeforeEach
    fun setUp() {
        val brl = Asset("BRL")

        // Add accounts to the Chart of Accounts
        runBlocking {
            val chartOfAccountsRepository = uowFactory.chartOfAccountsRepository
            chartOfAccountsRepository.add(
                Account(id = "account1", name = "Cash Account", type = AccountType.ASSET, asset = brl)
            )
            chartOfAccountsRepository.add(
                Account(id = "account2", name = "Revenue Account", type = AccountType.REVENUE, asset = brl)
            )
        }
    }

    @Test
    fun `should create and post a balanced transaction`() = runBlocking {
        val command = createValidPostTransactionCommand()
        val createdTransaction = ledgerPostingService.postTransaction(command)

        val savedTransaction = uowFactory.create().ledgerTransactionRepository().findById(createdTransaction.id)
        assertNotNull(savedTransaction)
        assertEquals(2, savedTransaction!!.entries.size)
        assertEquals("sourceTxId", savedTransaction.sourceTransactionId)

        // Verify that the event was saved to the outbox
        assertEquals(1, uowFactory.outboxRepository.events.size)
        val outboxEvent = uowFactory.outboxRepository.events.values.first()
        assertEquals(createdTransaction.id.toString(), outboxEvent.aggregateId)
        assertEquals("LedgerTransactionPosted", outboxEvent.eventType)

        // Deserialize the payload for robust, type-safe assertions
        val deserializer = KotlinxEventDeserializer()
        val deserializedEvent = deserializer.deserialize(outboxEvent)

        assertNotNull(deserializedEvent, "Event payload should be deserializable")
        assertTrue(deserializedEvent is LedgerTransactionPosted, "Event should be of the correct type")
        assertEquals(createdTransaction.id, deserializedEvent.transactionId)
        assertEquals(BigDecimal("100"), deserializedEvent.totalAmount)
        assertEquals(Asset("BRL"), deserializedEvent.asset)
    }

    @Test
    fun `should throw error for unbalanced transaction`() = runBlocking {
        val brl = Asset("BRL")
        val entries = listOf(
            LedgerEntry("account1", Amount(BigDecimal(100), brl), LedgerEntryType.DEBIT, "Debit entry"),
            LedgerEntry("account2", Amount(BigDecimal(50), brl), LedgerEntryType.CREDIT, "Credit entry")
        )

        val command = createValidPostTransactionCommand(entries = entries)
        val exception = assertFailsWith<IllegalArgumentException> {
            ledgerPostingService.postTransaction(command)
        }

        assertTrue(exception.message!!.startsWith("Ledger transaction is unbalanced."))
    }

    @Test
    fun `should throw error for non-existent accounts`() = runBlocking {
        val brl = Asset("BRL")
        val entries = listOf(
            LedgerEntry("account1", Amount(BigDecimal(100), brl), LedgerEntryType.DEBIT, "Debit entry"),
            LedgerEntry("account3", Amount(BigDecimal(50), brl), LedgerEntryType.CREDIT, "Credit entry"), // account3 does not exist
            LedgerEntry("account4", Amount(BigDecimal(50), brl), LedgerEntryType.CREDIT, "Credit entry")  // account4 does not exist
        )

        val command = createValidPostTransactionCommand(entries = entries)
        val exception = assertFailsWith<IllegalArgumentException> {
            ledgerPostingService.postTransaction(command)
        }

        val message = exception.message!!
        assertTrue(message.startsWith("Accounts do not exist or are not active in the Chart of Accounts:"))
        assertTrue(message.contains("account3"))
        assertTrue(message.contains("account4"))
    }

    @Test
    fun `should throw error for transaction with mixed assets`() = runBlocking {
        val brl = Asset("BRL")
        val usd = Asset("USD")

        val entries = listOf(
            LedgerEntry("account1", Amount(BigDecimal(100), brl), LedgerEntryType.DEBIT, "Debit entry"),
            LedgerEntry("account2", Amount(BigDecimal(100), usd), LedgerEntryType.CREDIT, "Credit entry")
        )

        val command = createValidPostTransactionCommand(entries = entries)
        val exception = assertFailsWith<IllegalArgumentException> {
            ledgerPostingService.postTransaction(command)
        }

        assertEquals("All entries in a transaction must have the same asset. Found mixed assets.", exception.message)
    }

    @Test
    fun `should return same result for duplicate transaction with same idempotency key`() {
        runBlocking {
            val key = IdempotencyKey()
            val command = createValidPostTransactionCommand().apply {
                idempotencyKey = key
            }

            // Post the transaction for the first time
            val firstResult = ledgerPostingService.postTransaction(command)
            assertNotNull(firstResult)

            // Verify one transaction was created
            assertEquals(1, uowFactory.outboxRepository.events.size)

            // Attempt to post the same transaction again with the same key
            // Use a different description to prove the operation is not re-executed
            val duplicateCommand = createValidPostTransactionCommand(description = "Different description").apply {
                idempotencyKey = key
            }
            val secondResult = ledgerPostingService.postTransaction(duplicateCommand)

            // Assert that the second result is the same as the first one
            assertEquals(firstResult.id, secondResult.id)
            assertEquals(firstResult.description, secondResult.description) // Should be the original description

            // Verify that no new transaction was created
            assertEquals(1, uowFactory.outboxRepository.events.size)
        }
    }


    private fun createValidPostTransactionCommand(
        sourceTransactionId: String = "sourceTxId",
        description: String = "Test transaction",
        entries: List<LedgerEntry> = listOf(
            LedgerEntry("account1", Amount(BigDecimal(100), Asset("BRL")), LedgerEntryType.DEBIT, "Debit entry"),
            LedgerEntry("account2", Amount(BigDecimal(100), Asset("BRL")), LedgerEntryType.CREDIT, "Credit entry")
        )
    ): PostTransactionCommand {
        return PostTransactionCommand(
            sourceTransactionId = sourceTransactionId,
            description = description,
            entries = entries
        )
    }
}
