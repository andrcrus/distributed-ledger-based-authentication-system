package net.andrc.flows

import co.paralleluniverse.fibers.Suspendable
import net.andrc.contracts.DeleteContainerContract
import net.andrc.contracts.PutContainerContract
import net.andrc.items.GeoData
import net.andrc.states.DeleteContainerState
import net.andrc.states.PutContainerState
import net.andrc.utils.ContainerIsExistsException
import net.corda.core.contracts.Command
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.StateAndRef
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
class DeleteContainerFlow(private val containerInfo: StateAndRef<PutContainerState>,
                          private val geoData: GeoData) : FlowLogic<SignedTransaction>() {
    override val progressTracker: ProgressTracker = tracker()
    companion object {
        object CREATING : ProgressTracker.Step("Creating a new a new delete container record!")
        object VERIFYING : ProgressTracker.Step("Verifying a delete container record!")
        object SUCCESS : ProgressTracker.Step("Create a delete container record!")
        fun tracker() = ProgressTracker(CREATING, VERIFYING, SUCCESS)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val otherFlowSession = initiateFlow(containerInfo.state.data.owner)
        serviceHub.vaultService.queryBy(PutContainerState::class.java).states.single{ it.state.data.containerName == containerInfo.state.data.containerName }
        progressTracker.currentStep = CREATING
        val tx = TransactionBuilder(notary)
                .addInputState(containerInfo)
                .addCommand(Command(PutContainerContract.Check(), listOf(containerInfo.state.data.owner.owningKey, ourIdentity.owningKey)))
                .addCommand(Command(DeleteContainerContract.Delete(), listOf(containerInfo.state.data.owner.owningKey, ourIdentity.owningKey)))
                .addOutputState(DeleteContainerState(containerInfo.state.data.containerName, containerInfo.state.data.owner,
                        participants = listOf(containerInfo.state.data.owner, ourIdentity), geoData = geoData))
        val signedRecord = serviceHub.signInitialTransaction(tx)
        progressTracker.currentStep = VERIFYING
        signedRecord.tx.toLedgerTransaction(serviceHub).verify()
        val allSignedTransaction = subFlow(CollectSignaturesFlow(signedRecord, listOf(otherFlowSession)))
        progressTracker.currentStep = SUCCESS
        return subFlow(FinalityFlow(allSignedTransaction, listOf(otherFlowSession)))
    }
}

@InitiatedBy(DeleteContainerFlow::class)
class DeleteContainerFlowReceiver(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an delete container transaction" using (output is DeleteContainerState)
                }
            }
        }
        val id = subFlow(signedTransactionFlow).id
        subFlow(ReceiveFinalityFlow(flowSession, id))
    }
}