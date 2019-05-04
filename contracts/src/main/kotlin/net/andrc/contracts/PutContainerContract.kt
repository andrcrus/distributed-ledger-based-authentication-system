package net.andrc.contracts

import net.andrc.items.Item
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

    private fun getItemsCapacity(items: List<Item>): Long {
        return items.stream().mapToLong{it.capacity}.sum()
    }

    override fun verify(tx: LedgerTransaction) = requireThat {
        tx.commands.requireSingleCommand<Send>()
        "There can be no inputs when register new container" using  (tx.inputs.isEmpty())
        val containerInfo = tx.outputs.single().data as PutContainerState
        "Container must be not empty" using (containerInfo.items.isNotEmpty())
        "Container capacity must be positive" using (containerInfo.maxCapacity > 0)
        "Container capacity must be greater then all items capacity" using (getItemsCapacity(containerInfo.items) <= containerInfo.maxCapacity)
    }

}