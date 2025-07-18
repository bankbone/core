package com.bankbone.core.ledger.domain

import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.Amount
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class LedgerEntryTest {
    private val brl = Asset("BRL")

    @Test
    fun `should create a valid debit entry`() {
        val entry = LedgerEntry("account1", Amount(BigDecimal(100), brl), LedgerEntryType.DEBIT, "Debit entry")
        assertEquals("account1", entry.accountId)
        assertEquals(BigDecimal(100), entry.amount.value)
        assertEquals(brl, entry.amount.asset)
        assertEquals(LedgerEntryType.DEBIT, entry.type)
        assertEquals("Debit entry", entry.description)
        assertEquals(brl, entry.asset)
    }

    @Test
    fun `should create a valid credit entry`() {
        val entry = LedgerEntry("account2", Amount(BigDecimal(200), brl), LedgerEntryType.CREDIT, "Credit entry")
        assertEquals("account2", entry.accountId)
        assertEquals(BigDecimal(200), entry.amount.value)
        assertEquals(brl, entry.amount.asset)
        assertEquals(LedgerEntryType.CREDIT, entry.type)
        assertEquals("Credit entry", entry.description)
        assertEquals(brl, entry.asset)
    }

    @Test
    fun `should throw error for zero amount`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            LedgerEntry("account1", Amount(BigDecimal.ZERO, brl), LedgerEntryType.DEBIT, "Invalid entry")
        }
        assertEquals("Amount must be positive", exception.message)
    }
}
