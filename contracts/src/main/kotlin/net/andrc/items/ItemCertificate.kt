package net.andrc.items

import net.corda.core.serialization.CordaSerializable
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*

/**
 * @author andrey.makhnov
 */
@CordaSerializable
data class ItemCertificate (
        val id: String = UUID.randomUUID().toString(),
        val date: Date = Date()
)