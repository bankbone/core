package com.bankbone.core.sharedkernel.application

interface ShardedCommand {
    /** A key used to route this command to a specific actor or shard. */
    val shardKey: String
}