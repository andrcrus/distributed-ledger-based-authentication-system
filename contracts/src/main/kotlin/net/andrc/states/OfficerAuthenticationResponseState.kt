package net.andrc.states

import net.andrc.contracts.OfficerAuthContract
import net.andrc.items.OfficerCertificate
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

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
        override val participants: List<AbstractParty>
) : ContractState

@CordaSerializable
enum class ResponseStatus {
    OK,
    FAILED
}