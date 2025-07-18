package com.bankbone.core.sharedkernel.di

import com.bankbone.core.ledger.application.LedgerPostingService
import com.bankbone.core.ledger.application.PostTransactionCommandValidator
import com.bankbone.core.ledger.infrastructure.InMemoryLedgerUnitOfWorkFactory
import com.bankbone.core.sharedkernel.infrastructure.InMemoryIdempotencyStore
import org.koin.dsl.module

val sharedKernelModule = module {
    single { InMemoryIdempotencyStore() }
}

val ledgerModule = module {
    single { InMemoryLedgerUnitOfWorkFactory() }
    single { PostTransactionCommandValidator() }
    single {
        LedgerPostingService(
            uowFactory = get(),
            validator = get(),
            idempotencyStore = get()
        )
    }
}