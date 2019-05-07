package net.andrc.items

import net.corda.core.serialization.CordaSerializable

/**
 * @author andrey.makhnov
 */
@CordaSerializable
data class Carrier(
        val organizationName: String,
        val carrierCertificate: CarrierCertificate
)