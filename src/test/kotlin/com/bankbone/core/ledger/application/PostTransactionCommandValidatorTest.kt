package com.bankbone.core.ledger.application

import com.bankbone.core.ledger.application.commands.PostTransactionCommand
import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountType
import com.bankbone.core.ledger.domain.LedgerEntry
import com.bankbone.core.ledger.domain.LedgerEntryType
import com.bankbone.core.ledger.infrastructure.InMemoryChartOfAccountsRepository
import com.bankbone.core.sharedkernel.domain.Amount
import com.bankbone.core.sharedkernel.domain.Asset
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PostTransactionCommandValidatorTest {

    private lateinit var chartOfAccountsRepository: InMemoryChartOfAccountsRepository
    private lateinit var validator: PostTransactionCommandValidator

    @BeforeEach
    fun setUp() {
        chartOfAccountsRepository = InMemoryChartOfAccountsRepository()
        validator = PostTransactionCommandValidator(chartOfAccountsRepository)

        runBlocking {
            chartOfAccountsRepository.add(
                Account(id = "account1", name = "Cash", type = AccountType.ASSET, asset = Asset("BRL"))
            )
        }
    }

    @Test
    fun `should pass for a valid command`() = runBlocking {
        val command = PostTransactionCommand(
            sourceTransactionId = "tx1",
            description = "Valid transaction",
            entries = listOf(
                LedgerEntry("account1", Amount(BigDecimal.TEN, Asset("BRL")), LedgerEntryType.DEBIT)
            )
        )
        validator.validate(command)
    }

    @Test
    fun `should throw error for blank source transaction ID`() = runBlocking {
        val command = PostTransactionCommand(" ", "Valid transaction", emptyList())
        val exception = assertFailsWith<IllegalArgumentException> {
            validator.validate(command)
        }
        assertEquals("Source transaction ID must not be blank.", exception.message)
    }

    @Test
    fun `should throw error for blank description`() = runBlocking {
        val command = PostTransactionCommand("tx1", "", emptyList())
        val exception = assertFailsWith<IllegalArgumentException> {
            validator.validate(command)
        }
        assertEquals("Description must not be blank.", exception.message)
    }

    @Test
    fun `should throw error for non-existent account`() = runBlocking {
        val command = PostTransactionCommand("tx1", "Invalid", listOf(LedgerEntry("non-existent", Amount(BigDecimal.TEN, Asset("BRL")), LedgerEntryType.DEBIT)))
        val exception = assertFailsWith<IllegalArgumentException> {
            validator.validate(command)
        }
        assertEquals("Accounts do not exist or are not active in the Chart of Accounts: non-existent", exception.message)
    }
}