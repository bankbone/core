package com.bankbone.core.ledger.ports

/**
 * A utility function to execute a block of code within a transactional
 * unit of work, ensuring commit on success and rollback on failure.
 */
suspend fun <T> LedgerUnitOfWork.transaction(block: suspend (LedgerUnitOfWork) -> T): T {
    return try {
        val result = block(this)
        this.commit()
        result
    } catch (e: Exception) {
        this.rollback()
        throw e
    }
}