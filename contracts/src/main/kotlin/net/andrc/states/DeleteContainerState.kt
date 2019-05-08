package net.andrc.states

import net.andrc.contracts.DeleteContainerContract
import net.andrc.items.GeoData
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
@BelongsToContract(DeleteContainerContract::class)
data class DeleteContainerState(
        val containerName: String,
        val owner: Party,
        val geoData: GeoData,
        override val participants: List<AbstractParty> = listOf(owner),
        val date: Date = Date()
) : ContractState