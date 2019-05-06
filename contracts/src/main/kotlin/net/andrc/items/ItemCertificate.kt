package net.andrc.items

import net.corda.core.serialization.CordaSerializable
import java.security.PrivateKey
import java.security.PublicKey
import java.time.LocalDate
import java.util.*

/**
 * @author andrey.makhnov
 */
@CordaSerializable
data class ItemCertificate (
        val  id: String = UUID.randomUUID().toString(),
        val  creationDate: Date = Date(),
        val  expirationDate: Date = Date(LocalDate.now().plusMonths(3).toEpochDay())
)