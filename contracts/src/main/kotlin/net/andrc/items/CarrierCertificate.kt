package net.andrc.items

import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.toBase58String
import java.security.PublicKey
import java.util.*

/**
 * @author andrey.makhnov
 */
@CordaSerializable
data class CarrierCertificate (
        val publicKey: PublicKey,
        val id: String = UUID.randomUUID().toString()
) {
    override fun toString(): String {
        return "OfficerCertificate(publicKey=${publicKey.toBase58String()}, id='$id')"
    }
}
