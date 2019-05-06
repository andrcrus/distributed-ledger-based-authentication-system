package net.andrc.items

import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 * @author andrey.makhnov
 */
@CordaSerializable
data class OfficerCertificate (
     val name: String,
     val organization: String,
     val id: String = UUID.randomUUID().toString()
)
