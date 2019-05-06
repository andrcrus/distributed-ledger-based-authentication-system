package net.andrc.contracts

import net.andrc.items.Item
import net.andrc.states.PutContainerState
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.TypeOnlyCommandData
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

    class Put : TypeOnlyCommandData()
    class Check : TypeOnlyCommandData()

    private fun getItemsCapacity(items: List<Item>): Long {
        return items.stream().mapToLong{it.capacity}.sum()
    }

    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.first { it.value == Put() || it.value == Check() }.value
        lateinit var containerInfo: ContractState
        containerInfo = if (tx.commands.size == 1 && command == Put()) {
            "There can be no inputs when register new container" using (tx.inputs.isEmpty())
            tx.outputs.single().data as PutContainerState
        }else {
            tx.inputs.single().state.data as PutContainerState
        }
        "Container must be not empty" using (containerInfo.items.isNotEmpty())
        "Container capacity must be positive" using (containerInfo.maxCapacity > 0)
        "Container capacity must be greater then all items capacity" using (getItemsCapacity(containerInfo.items) <= containerInfo.maxCapacity)
    }

}