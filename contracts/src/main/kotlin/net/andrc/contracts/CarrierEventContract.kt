package net.andrc.contracts

import net.andrc.states.CarrierEventState
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
class CarrierEventContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "net.andrc.contracts.CarrierEventContract"
    }

    class CarrierEvent : TypeOnlyCommandData()

    override fun verify(tx: LedgerTransaction) {
        tx.commands.requireSingleCommand<CarrierEvent>()
        "There can't be no inputs when carrier is changing" using  (tx.inputs.isEmpty())
        val output = tx.outputs.single().data as CarrierEventState
        "Date must be equals current" using (System.currentTimeMillis() - output.date.time <= 100000)
    }
}