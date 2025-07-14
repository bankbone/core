package com.bankbone.core.sharedkernel.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AmountTest {

    private val brl = Asset("BRL")
    private val usd = Asset("USD")

    @Test
    fun `should create amount with non-negative value`() {
        assertDoesNotThrow {
            Amount(BigDecimal(100), brl)
        }
        assertDoesNotThrow {
            Amount(BigDecimal.ZERO, brl)
        }
    }

    @Test
    fun `should throw error for negative amount value`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            Amount(BigDecimal(-100), brl)
        }
        assertEquals("Amount value must be non-negative", exception.message)
    }

    @Test
    fun `should correctly add two amounts of the same asset`() {
        val amount1 = Amount(BigDecimal(100), brl)
        val amount2 = Amount(BigDecimal(50), brl)
        val result = amount1 + amount2
        assertEquals(BigDecimal(150), result.value)
        assertEquals(brl, result.asset)
    }

    @Test
    fun `should throw error when adding amounts of different assets`() {
        val amount1 = Amount(BigDecimal(100), brl)
        val amount2 = Amount(BigDecimal(50), usd)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            amount1 + amount2
        }
        assertEquals("Cannot add amounts with different asset types", exception.message)
    }

    @Test
    fun `should correctly subtract two amounts of the same asset`() {
        val amount1 = Amount(BigDecimal(100), brl)
        val amount2 = Amount(BigDecimal(50), brl)
        val result = amount1 - amount2
        assertEquals(BigDecimal(50), result.value)
        assertEquals(brl, result.asset)
    }

    @Test
    fun `should throw error when subtracting amounts of different assets`() {
        val amount1 = Amount(BigDecimal(100), brl)
        val amount2 = Amount(BigDecimal(50), usd)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            amount1 - amount2
        }
        assertEquals("Cannot subtract amounts with different asset types", exception.message)
    }

    @Test
    fun `should create a zero amount using companion factory`() {
        val zeroAmount = Amount.zero(brl)
        assertEquals(BigDecimal.ZERO, zeroAmount.value)
        assertEquals(brl, zeroAmount.asset)
    }
}