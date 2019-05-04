package net.andrc.contracts

import net.andrc.states.DeleteContainerState
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class DeleteContainerContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "net.andrc.contracts.DeleteContainerContract"
    }

    class Delete : TypeOnlyCommandData()

    override fun verify(tx: LedgerTransaction) {
        tx.commands.requireSingleCommand<Delete>()
        "There can be no inputs when register new container" using  (tx.inputs.isEmpty())
        val output = tx.outputs.single().data as DeleteContainerState
    }
}