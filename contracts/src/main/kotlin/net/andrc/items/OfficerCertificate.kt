package net.andrc.items

import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.toBase58String
import java.security.PublicKey
import java.util.*

/**
 * @author andrey.makhnov
 */
@CordaSerializable
class OfficerCertificate (
     val name: String,
     val organization: String,
     val publicKey: PublicKey,
     val id: String = UUID.randomUUID().toString()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfficerCertificate

        if (name != other.name) return false
        if (organization != other.organization) return false
        if (publicKey != other.publicKey) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + organization.hashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "OfficerCertificate(name='$name', organization='$organization', publicKey=${publicKey.toBase58String()}, id='$id')"
    }
}
