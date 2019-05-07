package net.andrc.contracts

import net.andrc.states.OfficerAuthenticationRequestState
import net.andrc.utils.isValid
import net.andrc.utils.verifySign
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
/**
 * @author andrey.makhnov
 */
class OfficerAuthenticationRequestContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "net.andrc.contracts.OfficerAuthenticationRequestContract"
    }

    class Request : TypeOnlyCommandData()
    class Response : TypeOnlyCommandData()

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.first { it.value == Request() || it.value == Response() }.value
        if (command == Request()) {
            "There can be no inputs when officer create request" using  (tx.inputs.isEmpty())
            val output = tx.outputs.single().data as OfficerAuthenticationRequestState
            "Certificate must be valid" using (isValid(output.officerCertificate))
            "Signature must be valid" using (verifySign(output.data, output.signature, output.officerCertificate.publicKey))
        }
    }
}