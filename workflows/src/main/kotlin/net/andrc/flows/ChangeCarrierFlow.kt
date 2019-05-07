package net.andrc.flows

import co.paralleluniverse.fibers.Suspendable
import net.andrc.contracts.ChangeCarrierContract
import net.andrc.contracts.OfficerAuthContract
import net.andrc.states.ChangeCarrierState
import net.andrc.states.OfficerAuthenticationRequestState
import net.andrc.utils.isValid
import net.andrc.utils.verifySign
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
class ChangeCarrierFlow(private val changeCarrierState: ChangeCarrierState) : FlowLogic<SignedTransaction>() {
    override val progressTracker: ProgressTracker = tracker()
    companion object {
        object CREATING : ProgressTracker.Step("Creating a change carrier record!")
        object VERIFYING : ProgressTracker.Step("Verifying a change carrier record!")
        object SUCCESS : ProgressTracker.Step("Create the a change carrier record!")
        fun tracker() = ProgressTracker(CREATING, VERIFYING, SUCCESS)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val sessions = changeCarrierState.participants.map { initiateFlow(it as Party) }
        progressTracker.currentStep = CREATING
        val tx = TransactionBuilder(notary)
                .addCommand(Command(ChangeCarrierContract.Change(), changeCarrierState.participants.map { it.owningKey }))
                .addOutputState(changeCarrierState)
        val signedRecord = serviceHub.signInitialTransaction(tx)
        progressTracker.currentStep = VERIFYING
        signedRecord.tx.toLedgerTransaction(serviceHub).verify()
        val allSignedTransaction = subFlow(CollectSignaturesFlow(signedRecord, sessions))
        progressTracker.currentStep = SUCCESS
        return subFlow(FinalityFlow(allSignedTransaction, sessions))
    }
}

@InitiatedBy(ChangeCarrierFlow::class)
class ChangeCarrierReceiver(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                requireThat {
                    "This must be an  officer authentication request transaction" using (stx.tx.outputs.single().data is ChangeCarrierState)
                    val output = stx.tx.outputs.single().data as ChangeCarrierState
                    "Signature must be valid" using (verifySign(output.data, output.signature, output.carrier.carrierCertificate.publicKey))
                }
            }
        }
        val id = subFlow(signedTransactionFlow).id
        subFlow(ReceiveFinalityFlow(flowSession, id))
    }
}