package com.bankbone.core.sharedkernel.domain

import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

abstract class AggregateId(
    @get:JsonValue
    open val value: UUID
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AggregateId
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()
}