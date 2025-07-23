package com.bankbone.core.ledger.domain

import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.Amount
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class LedgerTransactionTest {

    private val brl = Asset("BRL")
    private val usd = Asset("USD")
    private val accountId1 = Account.Id.random()
    private val accountId2 = Account.Id.random()

    @Test
    fun `should create a valid balanced transaction`() {
        val entries = listOf(
            LedgerEntry(accountId1, Amount(BigDecimal(100), brl), LedgerEntryType.DEBIT),
            LedgerEntry(accountId2, Amount(BigDecimal(100), brl), LedgerEntryType.CREDIT)
        )

        assertDoesNotThrow {
            LedgerTransaction(
                id = LedgerTransaction.Id.random(),
                sourceTransactionId = "sourceTxId",
                description = "Valid transaction",
                entries = entries
            )
        }
    }

    @Test
    fun `should throw error for unbalanced transaction`() {
        val entries = listOf(
            LedgerEntry(accountId1, Amount(BigDecimal(100), brl), LedgerEntryType.DEBIT),
            LedgerEntry(accountId2, Amount(BigDecimal(50), brl), LedgerEntryType.CREDIT)
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            LedgerTransaction(LedgerTransaction.Id.random(), "sourceTxId", "Unbalanced", entries)
        }
        assertTrue(exception.message!!.startsWith("Ledger transaction is unbalanced."))
    }

    @Test
    fun `should throw error for transaction with less than two entries`() {
        val entries = listOf(
            LedgerEntry(accountId1, Amount(BigDecimal(100), brl), LedgerEntryType.DEBIT)
        )
        val exception = assertThrows(IllegalArgumentException::class.java) {
            LedgerTransaction(LedgerTransaction.Id.random(), "sourceTxId", "Single entry", entries)
        }
        assertEquals("A balanced ledger transaction must have at least two entries.", exception.message)
    }

    @Test
    fun `should throw error for transaction with mixed assets`() {
        val entries = listOf(
            LedgerEntry(accountId1, Amount(BigDecimal(100), brl), LedgerEntryType.DEBIT),
            LedgerEntry(accountId2, Amount(BigDecimal(100), usd), LedgerEntryType.CREDIT)
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            LedgerTransaction(LedgerTransaction.Id.random(), "sourceTxId", "Mixed assets", entries)
        }
        assertEquals("All entries in a transaction must have the same asset. Found mixed assets.", exception.message)
    }
}
