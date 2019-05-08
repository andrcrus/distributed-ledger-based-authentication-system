package net.andrc.flows

import co.paralleluniverse.fibers.Suspendable
import net.andrc.contracts.ChangeCarrierContract
import net.andrc.states.CarrierEventState
import net.andrc.states.ChangeCarrierState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
/**
 * @author andrey.makhnov
 */
@StartableByRPC
@InitiatingFlow
class CarrierEventFlow(private val carrierEventState: CarrierEventState) : FlowLogic<Unit>() {
    override val progressTracker: ProgressTracker = tracker()
    companion object {
        object CREATING : ProgressTracker.Step("")
        object VERIFYING : ProgressTracker.Step("")
        object SUCCESS : ProgressTracker.Step("")
        fun tracker() = ProgressTracker(CREATING, VERIFYING, SUCCESS)
    }

    @Suspendable
    override fun call() {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val sessions = carrierEventState.participants.filter { it != ourIdentity }.map { initiateFlow(it as Party) }
        progressTracker.currentStep = CREATING
        val tx = TransactionBuilder(notary)
                .addCommand(Command(ChangeCarrierContract.Change(), carrierEventState.participants.map { it.owningKey }))
                .addOutputState(carrierEventState)
        val signedRecord = serviceHub.signInitialTransaction(tx)
        progressTracker.currentStep = VERIFYING
        signedRecord.tx.toLedgerTransaction(serviceHub).verify()
        val allSignedTransaction = subFlow(CollectSignaturesFlow(signedRecord, sessions))
        progressTracker.currentStep = SUCCESS
        subFlow(FinalityFlow(allSignedTransaction, sessions))
    }
}

@InitiatedBy(CarrierEventFlow::class)
class CarrierEventFlowReceiver(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                requireThat {
                    "This must be an  officer authentication request transaction" using (stx.tx.outputs.single().data is CarrierEventState)
                    stx.tx.outputs.single().data as ChangeCarrierState
                }
            }
        }
        val id = subFlow(signedTransactionFlow).id
        subFlow(ReceiveFinalityFlow(flowSession, id))
    }
}
