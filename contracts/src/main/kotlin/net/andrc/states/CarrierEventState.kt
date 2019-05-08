package net.andrc.states

import net.andrc.contracts.CarrierEventContract
import net.andrc.items.Events
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import java.util.*

// *********
// * State *
// *********
/**
 * @author andrey.makhnov
 */
@BelongsToContract(CarrierEventContract::class)
data class CarrierEventState(
     val eventContract: Events,
     override val participants: List<AbstractParty>,
     val date: Date = Date()
) : ContractState {
    override fun toString(): String {
        return """
            |
            | "carrierEvent" : {
            | "event" : "$eventContract",
            | "date"  : "$date"
            | }
            |
        """.trimMargin()
    }
}