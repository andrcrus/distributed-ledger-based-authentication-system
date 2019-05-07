package net.andrc.states

import net.andrc.contracts.OfficerAuthenticationRequestContract
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
@BelongsToContract(OfficerAuthenticationRequestContract::class)
data class OfficerAuthenticationRequestState(
        val officerCertificate: OfficerCertificate,
        val data: String,
        val signature: String,
        val owners: List<Party>,
        override val participants: List<AbstractParty>,
        val requestId: String = UUID.randomUUID().toString()
) : ContractState