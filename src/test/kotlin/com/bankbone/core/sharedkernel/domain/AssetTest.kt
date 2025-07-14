package com.bankbone.core.sharedkernel.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AssetTest {

    @Test
    fun `should create asset with non-blank code`() {
        val asset = Asset("BRL")
        assertEquals("BRL", asset.code)
    }

    @Test
    fun `should throw error for blank asset code`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            Asset(" ")
        }
        assertEquals("Asset code must not be blank.", exception.message)
    }

    @Test
    fun `should throw error for empty asset code`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            Asset("")
        }
        assertEquals("Asset code must not be blank.", exception.message)
    }

    @Test
    fun `toString should return the asset code`() {
        val asset = Asset("USD")
        assertEquals("USD", asset.toString())
    }
}