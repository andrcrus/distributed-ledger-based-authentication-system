package net.andrc.states

import net.andrc.contracts.OfficerAuthContract
import net.andrc.items.GeoData
import net.andrc.items.OfficerCertificate
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.util.*

// *********
// * State *
// *********
/**
 * @author andrey.makhnov
 */
@BelongsToContract(OfficerAuthContract::class)
data class OfficerAuthenticationRequestState(
        val officerCertificate: OfficerCertificate,
        val data: String,
        val signature: String,
        val owners: List<Party>,
        val geoData: GeoData,
        override val participants: List<AbstractParty>,
        val requestId: String = UUID.randomUUID().toString(),
        val date: Date = Date()
) : ContractState