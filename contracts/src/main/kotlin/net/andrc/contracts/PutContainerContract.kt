package net.andrc.contracts

import net.andrc.states.PutContainerState
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class PutContainerContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "net.andrc.contracts.PutContainerContract"
    }

    class Send : TypeOnlyCommandData()

    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<Send>()
        "There can be no inputs when register new container" using  (tx.inputs.isEmpty())
        val containerInfo = tx.outputs.single().data as PutContainerState
        "Container must be not empty" using (containerInfo.container.getImmutableItems().isEmpty() && containerInfo.container.getImmutableContainers().isNotEmpty())
        "Container must be not empty" using (containerInfo.container.getImmutableItems().isNotEmpty() && containerInfo.container.getImmutableContainers().isEmpty())
        "Container capacity must be positive" using (containerInfo.container.maxCapacity > 0)
    }

}