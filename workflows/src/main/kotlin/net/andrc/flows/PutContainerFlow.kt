package net.andrc.flows

import co.paralleluniverse.fibers.Suspendable
import net.andrc.contracts.PutContainerContract
import net.andrc.states.PutContainerState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
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
class PutContainerFlow(private val containerInfo: PutContainerState): FlowLogic<SignedTransaction>() {
    override val progressTracker: ProgressTracker = tracker()
    companion object {
        object CREATING : ProgressTracker.Step("Creating a new container record!")
        object VERIFYING : ProgressTracker.Step("Verifying the container record!")
        object SUCCESS : ProgressTracker.Step("Create the container record!")
        fun tracker() = ProgressTracker(CREATING, VERIFYING, SUCCESS)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val otherFlowSession = initiateFlow(containerInfo.owner)
        progressTracker.currentStep = CREATING
        val tx = TransactionBuilder(notary)
                .addCommand(Command(PutContainerContract.Put(), listOf(containerInfo.owner.owningKey, ourIdentity.owningKey)))
                .addOutputState(containerInfo)
        val signedRecord = serviceHub.signInitialTransaction(tx)
        progressTracker.currentStep = VERIFYING
        signedRecord.tx.toLedgerTransaction(serviceHub).verify()
        val allSignedTransaction = subFlow(CollectSignaturesFlow(signedRecord, listOf(otherFlowSession)))
        progressTracker.currentStep = SUCCESS
        return subFlow(FinalityFlow(allSignedTransaction, listOf(otherFlowSession)))
    }
}

@InitiatedBy(PutContainerFlow::class)
class PutContainerFlowReceiver(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an put container transaction" using (output is PutContainerState)
                }
            }
        }
        val id = subFlow(signedTransactionFlow).id
        subFlow(ReceiveFinalityFlow(flowSession, id))
    }
}





