package com.bankbone.core.ledger.domain

import com.bankbone.core.sharedkernel.domain.Asset
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AccountTest {

    private val brl = Asset("BRL")

    @Test
    fun `should create an account successfully with valid data`() {
        val account = Account(
            id = Account.Id.random(),
            name = "Valid Account",
            type = AccountType.ASSET,
            asset = brl
        )
        assertEquals("Valid Account", account.name)
        assertTrue(account.isActive)
    }

    @Test
    fun `should fail to create an account with a blank name`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Account(
                id = Account.Id.random(),
                name = " ",
                type = AccountType.ASSET,
                asset = brl
            )
        }
        assertEquals("Account name must not be blank", exception.message)
    }

    // Note: This test reflects data class equality. For DDD Entities, equality is typically based only on ID.
    // If Account becomes a full Entity again, this test should be changed.
    @Test
    fun `should consider two accounts with the same data as equal`() {
        val id = Account.Id.random()
        val account1 = Account(id, "Cash", AccountType.ASSET, brl, isActive = true)
        val account2 = Account(id, "Cash", AccountType.ASSET, brl, isActive = true)

        assertEquals(account1, account2)
        assertEquals(account1.hashCode(), account2.hashCode())
    }

    @Test
    fun `should consider two accounts with different data as not equal`() {
        val id = Account.Id.random()
        val account1 = Account(id, "Cash", AccountType.ASSET, brl)
        val account2 = Account(id, "Cash Renamed", AccountType.ASSET, brl)

        assertNotEquals(account1, account2)
    }
}
