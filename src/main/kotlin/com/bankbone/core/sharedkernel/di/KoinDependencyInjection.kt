package com.bankbone.core.sharedkernel.di

import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

class KoinDependencyInjection : DependencyInjection {
    private val koin: Koin by lazy { KoinPlatformTools.defaultContext().get() }

    override fun <T : Any> get(clazz: Class<T>): T {
        return koin.get(clazz.kotlin, null, null)
    }

    @OptIn(KoinInternalApi::class)
    override fun <T : Any> getAll(clazz: Class<T>): List<T> {
        // We get the root scope to explicitly call the non-reified getAll function,
        // avoiding the compiler's overload resolution ambiguity with the reified extension.
        return koin.scopeRegistry.rootScope.getAll(clazz.kotlin)
    }

    companion object {
        /**
         * Initializes Koin with the given application declaration.
         */
        fun init(appDeclaration: KoinAppDeclaration = {}) {
            if (KoinPlatformTools.defaultContext().getOrNull() == null) {
                startKoin(appDeclaration)
            }
        }
    }
}