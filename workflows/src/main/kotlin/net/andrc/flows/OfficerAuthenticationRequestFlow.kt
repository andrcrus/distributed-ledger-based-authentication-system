package net.andrc.flows

import co.paralleluniverse.fibers.Suspendable
import net.andrc.contracts.OfficerAuthenticationRequestContract
import net.andrc.states.OfficerAuthenticationRequestState
import net.andrc.utils.isValid
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
class OfficerAuthenticationRequestFlow(private val officerAuthenticationRequestState: OfficerAuthenticationRequestState) : FlowLogic<String>() {
    override val progressTracker: ProgressTracker = tracker()
    companion object {
        object CREATING : ProgressTracker.Step("Creating a new container record!")
        object VERIFYING : ProgressTracker.Step("Verifying the container record!")
        object SUCCESS : ProgressTracker.Step("Create the container record!")
        fun tracker() = ProgressTracker(CREATING, VERIFYING, SUCCESS)
    }

    @Suspendable
    override fun call(): String {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val sessions = officerAuthenticationRequestState.owners.map { initiateFlow(it) }
        progressTracker.currentStep = CREATING
        val tx = TransactionBuilder(notary)
                .addCommand(Command(OfficerAuthenticationRequestContract.Request(), listOf(ourIdentity.owningKey)))
                .addOutputState(officerAuthenticationRequestState)
        val signedRecord = serviceHub.signInitialTransaction(tx)
        progressTracker.currentStep = VERIFYING
        signedRecord.tx.toLedgerTransaction(serviceHub).verify()
        val allSignedTransaction = subFlow(CollectSignaturesFlow(signedRecord, sessions))
        progressTracker.currentStep = SUCCESS
        subFlow(FinalityFlow(allSignedTransaction, sessions))
        return officerAuthenticationRequestState.requestId
    }
}

@InitiatedBy(OfficerAuthenticationRequestFlow::class)
class OfficerAuthenticationRequestReceiver(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an  officer authentication request transaction" using (output is OfficerAuthenticationRequestState)
                    "Certificate must be valid" using (isValid((output as OfficerAuthenticationRequestState).officerCertificate))
                }
            }
        }
        val id = subFlow(signedTransactionFlow).id
        subFlow(ReceiveFinalityFlow(flowSession, id))
    }
}
