package com.bankbone.core.sharedkernel.domain

import kotlinx.serialization.Serializable

@Serializable
data class Asset(val code: String) {
    init {
        require(code.isNotBlank()) { "Asset code must not be blank." }
    }

    override fun toString(): String {
        return code
    }
}