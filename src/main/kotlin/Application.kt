package com.bankbone.core

import com.bankbone.core.ledger.application.LedgerPostingService
import com.bankbone.core.sharedkernel.di.DependencyInjection
import com.bankbone.core.sharedkernel.di.get
import com.bankbone.core.sharedkernel.di.KoinDependencyInjection
import com.bankbone.core.sharedkernel.di.ledgerModule
import com.bankbone.core.sharedkernel.di.sharedKernelModule
import io.ktor.server.application.*
import org.koin.dsl.module

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    KoinDependencyInjection.init {
        modules(sharedKernelModule, ledgerModule)
    }

    val di: DependencyInjection = KoinDependencyInjection()
    val ledgerService: LedgerPostingService = di.get<LedgerPostingService>()
    // Now you can use ledgerService in your application
}