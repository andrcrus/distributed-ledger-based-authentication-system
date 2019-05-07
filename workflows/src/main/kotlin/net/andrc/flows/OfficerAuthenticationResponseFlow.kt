package net.andrc.flows

import co.paralleluniverse.fibers.Suspendable
import net.andrc.contracts.OfficerAuthContract
import net.andrc.states.OfficerAuthenticationRequestState
import net.andrc.states.OfficerAuthenticationResponseState
import net.andrc.utils.isValid
import net.andrc.utils.verifySign
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
class OfficerAuthenticationResponseFlow(private val officerAuthenticationResponseState: OfficerAuthenticationResponseState) : FlowLogic<SignedTransaction>() {
    override val progressTracker: ProgressTracker = tracker()
    companion object {
        object CREATING : ProgressTracker.Step("Creating a new officer authentication request record!")
        object VERIFYING : ProgressTracker.Step("Verifying a  new officer authentication request record!")
        object SUCCESS : ProgressTracker.Step("Create a new officer authentication request record!")
        fun tracker() = ProgressTracker(CREATING, VERIFYING, SUCCESS)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val inputState = serviceHub.vaultService.queryBy(OfficerAuthenticationRequestState::class.java)
                .states.single { it.state.data.requestId == officerAuthenticationResponseState.requestId }
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val sessions = officerAuthenticationResponseState.owners.map { initiateFlow(it) }
        progressTracker.currentStep = CREATING
        val tx = TransactionBuilder(notary)
                .addCommand(Command(OfficerAuthContract.Response(), listOf(ourIdentity.owningKey)))
                .addOutputState(officerAuthenticationResponseState)
                .addInputState(inputState)
        val signedRecord = serviceHub.signInitialTransaction(tx)
        progressTracker.currentStep = VERIFYING
        signedRecord.tx.toLedgerTransaction(serviceHub).verify()
        val allSignedTransaction = subFlow(CollectSignaturesFlow(signedRecord, sessions))
        progressTracker.currentStep = SUCCESS
        return subFlow(FinalityFlow(allSignedTransaction, sessions))

    }
}

@InitiatedBy(OfficerAuthenticationResponseFlow::class)
class OfficerAuthenticationResponseReceiver(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an  officer authentication request transaction" using (output is OfficerAuthenticationRequestState)
                    "Certificate must be valid" using (isValid((output as OfficerAuthenticationResponseState).officerCertificate))
                    "Signature must be valid" using (verifySign(output.data, output.signature, output.officerCertificate.publicKey))
                }
            }
        }
        val id = subFlow(signedTransactionFlow).id
        subFlow(ReceiveFinalityFlow(flowSession, id))
    }
}
