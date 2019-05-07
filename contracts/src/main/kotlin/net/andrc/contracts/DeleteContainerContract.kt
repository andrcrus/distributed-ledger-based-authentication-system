package net.andrc.contracts

import net.andrc.states.DeleteContainerState
import net.andrc.states.PutContainerState
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
/**
 * @author andrey.makhnov
 */
class DeleteContainerContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "net.andrc.contracts.DeleteContainerContract"
    }

    class Delete : TypeOnlyCommandData()

    override fun verify(tx: LedgerTransaction) {
        tx.commands.first{ it.value == Delete() }
        "There can't be no inputs when register new container" using  (tx.inputs.isNotEmpty())
        val output = tx.outputs.single().data as DeleteContainerState
        val input = tx.inputs.single().state.data as PutContainerState
        "Names must be equals" using (output.containerName == input.containerName)
    }
}