package com.bankbone.core.ledger.domain.serializers

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.sharedkernel.infrastructure.serialization.AggregateIdSerializer

object AccountIdSerializer : AggregateIdSerializer<Account.Id>({ uuid -> Account.Id(uuid) })
