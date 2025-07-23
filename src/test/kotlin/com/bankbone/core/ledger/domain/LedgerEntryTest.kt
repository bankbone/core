package com.bankbone.core.ledger.domain

import com.bankbone.core.sharedkernel.domain.Amount
import com.bankbone.core.sharedkernel.domain.Asset
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LedgerEntryTest {

    private val brl = Asset("BRL")
    private val accountId = Account.Id.random()

    @Test
    fun `should create a ledger entry successfully`() {
        val entry = LedgerEntry(accountId, Amount(BigDecimal.TEN, brl), LedgerEntryType.DEBIT)
        assertEquals(accountId, entry.accountId)
        assertEquals(brl, entry.asset)
    }

    @Test
    fun `should fail to create a ledger entry with a zero amount`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            LedgerEntry(accountId, Amount(BigDecimal.ZERO, brl), LedgerEntryType.CREDIT)
        }
        assertEquals("Amount must be positive", exception.message)
    }
}