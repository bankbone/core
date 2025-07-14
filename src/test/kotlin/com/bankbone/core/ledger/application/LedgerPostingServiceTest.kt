package com.bankbone.core.ledger.application

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountType
import com.bankbone.core.ledger.domain.LedgerEntry
import com.bankbone.core.ledger.domain.LedgerEntryType
import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.ledger.infrastructure.InMemoryChartOfAccountsRepository
import com.bankbone.core.ledger.infrastructure.InMemoryLedgerTransactionRepository
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.Amount
import com.bankbone.core.sharedkernel.infrastructure.InMemoryDomainEventPublisher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*
import java.math.BigDecimal

class LedgerPostingServiceTest {

    private lateinit var ledgerTransactionRepository: InMemoryLedgerTransactionRepository
    private lateinit var chartOfAccountsRepository: InMemoryChartOfAccountsRepository
    private lateinit var domainEventPublisher: InMemoryDomainEventPublisher
    private lateinit var ledgerPostingService: LedgerPostingService

    @BeforeEach
    fun setUp() {
        ledgerTransactionRepository = InMemoryLedgerTransactionRepository()
        chartOfAccountsRepository = InMemoryChartOfAccountsRepository()
        domainEventPublisher = InMemoryDomainEventPublisher()
        ledgerPostingService = LedgerPostingService(ledgerTransactionRepository, chartOfAccountsRepository, domainEventPublisher)

        val brl = Asset("BRL")

        // Add accounts to the Chart of Accounts
        runBlocking {
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
        val brl = Asset("BRL")
        val entries = listOf(
            LedgerEntry("account1", Amount(BigDecimal(100), brl), LedgerEntryType.DEBIT, "Debit entry"),
            LedgerEntry("account2", Amount(BigDecimal(100), brl), LedgerEntryType.CREDIT, "Credit entry")
        )

        val createdTransaction = ledgerPostingService.createAndPostTransaction(
            sourceTransactionId = "sourceTxId",
            description = "Test transaction",
            entries = entries
        )

        val savedTransaction = ledgerTransactionRepository.findById(createdTransaction.id)
        assertNotNull(savedTransaction)
        assertEquals(2, savedTransaction!!.entries.size)
        assertEquals("sourceTxId", savedTransaction.sourceTransactionId)

        // Verify that the domain event was published
        assertEquals(1, domainEventPublisher.publishedEvents.size)
        val event = domainEventPublisher.publishedEvents.first() as LedgerTransactionPosted
        assertEquals(createdTransaction.id, event.transactionId)
        assertEquals(BigDecimal(100), event.totalAmount)
        assertEquals(brl, event.asset)
    }

    @Test
    fun `should throw error for unbalanced transaction`() = runBlocking {
        val brl = Asset("BRL")
        val entries = listOf(
            LedgerEntry("account1", Amount(BigDecimal(100), brl), LedgerEntryType.DEBIT, "Debit entry"),
            LedgerEntry("account2", Amount(BigDecimal(50), brl), LedgerEntryType.CREDIT, "Credit entry")
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            ledgerPostingService.createAndPostTransaction(sourceTransactionId = "sourceTxId",
                description = "Test transaction",
                entries = entries)
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

        val exception = assertFailsWith<IllegalArgumentException> {
            ledgerPostingService.createAndPostTransaction(sourceTransactionId = "sourceTxId",
                description = "Test transaction",
                entries = entries)
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

        val exception = assertFailsWith<IllegalArgumentException> {
            ledgerPostingService.createAndPostTransaction(sourceTransactionId = "sourceTxId",
                description = "Test transaction",
                entries = entries)
        }

        assertEquals("All entries in a transaction must have the same asset. Found mixed assets.", exception.message)
    }
}
