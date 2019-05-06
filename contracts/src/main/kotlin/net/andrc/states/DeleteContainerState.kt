package net.andrc.states

import net.andrc.contracts.DeleteContainerContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party


@BelongsToContract(DeleteContainerContract::class)
data class DeleteContainerState(
        val containerName: String,
        val owner: Party,
        override val participants: List<AbstractParty> = listOf(owner)
) : ContractState