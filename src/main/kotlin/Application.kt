package com.bankbone.core

import com.bankbone.core.sharedkernel.di.KoinDependencyInjection
import com.bankbone.core.sharedkernel.di.applicationModule
import com.bankbone.core.sharedkernel.di.productionPersistenceModule
import com.bankbone.core.sharedkernel.di.sharedKernelModule
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    KoinDependencyInjection.init {
        // Load modules for the production environment
        modules(sharedKernelModule, applicationModule, productionPersistenceModule)
    }
    // You can now get services in your Ktor routes using Koin's Ktor plugin:
    // val ledgerService by inject<LedgerPostingService>()
}