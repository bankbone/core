package com.bankbone.core.ledger.application.commands

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.AccountId
import com.bankbone.core.ledger.domain.AccountType
import com.bankbone.core.sharedkernel.application.IdempotentCommand
import com.bankbone.core.sharedkernel.application.ShardedCommand
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.IdempotencyKey

/**
 * Command to create a new account in the chart of accounts.
 *
 * @property name The name of the account
 * @property type The type of the account (e.g., ASSET, LIABILITY)
 * @property asset The asset type for the account
 * @property parentAccountId Optional ID of the parent account
 * @property metadata Additional metadata for the account
 * @property idempotencyKey Key to ensure idempotency of the operation
 * @property shardKeyOverride Optional override for the shard key used in distributed systems
 */
data class CreateAccountCommand(
    val name: String,
    val type: AccountType,
    val asset: Asset,
    val parentAccountId: AccountId? = null,
    val metadata: Map<String, String> = emptyMap(),
    override var idempotencyKey: IdempotencyKey = IdempotencyKey(),
    private val shardKeyOverride: String? = null
) : IdempotentCommand, ShardedCommand {
    
    /**
     * The shard key determines how commands are distributed in a sharded system.
     * By default, it uses the parent account ID or account type as the shard key.
     */
    override val shardKey: String
        get() = shardKeyOverride ?: parentAccountId?.toString() ?: type.name
}