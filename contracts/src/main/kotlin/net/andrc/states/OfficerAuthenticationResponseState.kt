package net.andrc.states

import net.andrc.contracts.OfficerAuthContract
import net.andrc.items.GeoData
import net.andrc.items.OfficerCertificate
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.util.*

// *********
// * State *
// *********
/**
 * @author andrey.makhnov
 */
@BelongsToContract(OfficerAuthContract::class)
data class OfficerAuthenticationResponseState(
        val officerCertificate: OfficerCertificate,
        val data: String,
        val signature: String,
        val owners: List<Party>,
        val result: ResponseStatus,
        val requestId: String,
        val geoData: GeoData,
        override val participants: List<AbstractParty>,
        val date: Date = Date()
) : ContractState {
    override fun toString(): String {
        return """
            |
            |"officerAuthenticationResponseState" : {
            |     "officerCertificate" : $officerCertificate,
            |      "result" : "$result",
            |      "requestId" : "$requestId",
            |      "geoData" : "$geoData",
            |      "date" : "$date"
            |}
            |
        """.trimMargin()
    }
}

@CordaSerializable
enum class ResponseStatus {
    OK,
    FAILED
}