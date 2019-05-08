package net.andrc.states

import net.andrc.contracts.PutContainerContract
import net.andrc.items.GeoData
import net.andrc.items.Item
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
@BelongsToContract(PutContainerContract::class)
data class PutContainerState(
        val containerName: String,
        val maxCapacity: Long,
        val items: List<Item>,
        val containers: List<String>,
        val owner: Party,
        val geoData: GeoData,
        override val participants: List<AbstractParty> = listOf(owner),
        val date: Date = Date()
) : ContractState {
    override fun toString(): String {
        return """
            |
            |"container" : {
            | "containerName" : "$containerName",
            | "maxCapacity" : "$maxCapacity",
            | "items" : ${items.map { it.getItemInfo() }},
            | "containers" : "$containers",
            | "geo" : $geoData,
            | "date" : "$date"
            |}
            |
        """.trimMargin()
    }
}