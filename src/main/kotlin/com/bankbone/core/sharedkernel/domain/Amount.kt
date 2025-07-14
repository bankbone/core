package com.bankbone.core.sharedkernel.domain

import java.math.BigDecimal

data class Amount(
    val value: BigDecimal,
    val asset: Asset // Represents the currency or asset type (e.g., "BRL", "USD")
) {
    init {
        require(value >= BigDecimal.ZERO) { "Amount value must be non-negative" }
    }

    operator fun plus(other: Amount): Amount {
        require(asset == other.asset) { "Cannot add amounts with different asset types" }
        return Amount(value + other.value, asset)
    }

    operator fun minus(other: Amount): Amount {
        require(asset == other.asset) { "Cannot subtract amounts with different asset types" }
        return Amount(value - other.value, asset)
    }

    operator fun times(multiplier: BigDecimal): Amount {
        return Amount(value * multiplier, asset)
    }

    companion object {
        fun zero(asset: Asset): Amount = Amount(BigDecimal.ZERO, asset)
    }
}