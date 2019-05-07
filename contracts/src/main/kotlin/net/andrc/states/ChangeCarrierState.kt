package net.andrc.states

import net.andrc.contracts.ChangeCarrierContract
import net.andrc.items.Carrier
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import java.util.*


// *********
// * State *
// *********
/**
 * @author andrey.makhnov
 */
@BelongsToContract(ChangeCarrierContract::class)
data class ChangeCarrierState(
        val carrier: Carrier,
        val data: String,
        val signature: String,
        override val participants: List<AbstractParty>,
        val date: Date = Date()
) : ContractState