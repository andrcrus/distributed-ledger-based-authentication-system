package net.andrc.flows

import co.paralleluniverse.fibers.Suspendable
import net.andrc.contracts.PutContainerContract
import net.andrc.states.PutContainerState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
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
        object VERIFYING_FAILED : ProgressTracker.Step("Verifying is failed")
        object SUCCESS : ProgressTracker.Step("Create the container record!")
        fun tracker() = ProgressTracker(CREATING, VERIFYING, VERIFYING_FAILED, SUCCESS)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        progressTracker.currentStep = CREATING
        val tx = TransactionBuilder(notary)
                .addCommand(Command(PutContainerContract.Send(), listOf(containerInfo.owner.owningKey)))
                .addOutputState(containerInfo)
        val signedRecord = serviceHub.signInitialTransaction(tx)
        progressTracker.currentStep = VERIFYING
        try {
            signedRecord.tx.toLedgerTransaction(serviceHub).verify()
        }
        catch (e: Exception) {
            progressTracker.currentStep = VERIFYING_FAILED
            throw e
        }
        progressTracker.currentStep = SUCCESS
        return subFlow(FinalityFlow(signedRecord))
    }
}





