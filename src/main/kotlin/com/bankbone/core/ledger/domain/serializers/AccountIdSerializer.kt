package com.bankbone.core.ledger.domain.serializers

import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.sharedkernel.infrastructure.serialization.UuidBasedIdSerializer

object AccountIdSerializer : UuidBasedIdSerializer<Account.Id>(
    serialName = "com.bankbone.core.ledger.domain.Account.Id",
    fromUuid = { uuid -> Account.Id(uuid) }
)
