package net.andrc.states

import net.andrc.contracts.PutContainerContract
import net.andrc.items.Container
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * State *
// *********
@BelongsToContract(PutContainerContract::class)
data class PutContainerState(
        val container: Container,
        val owner: Party = container.owner,
        override val participants: List<AbstractParty> = listOf(owner)
) : ContractState
