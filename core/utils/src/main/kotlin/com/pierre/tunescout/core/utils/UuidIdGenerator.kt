package com.pierre.tunescout.core.utils

import java.util.UUID

class UuidIdGenerator : IdGenerator {
    override fun createId(): String = UUID.randomUUID().toString()
}
