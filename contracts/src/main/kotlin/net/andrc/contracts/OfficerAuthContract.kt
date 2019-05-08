package net.andrc.contracts

import net.andrc.states.OfficerAuthenticationRequestState
import net.andrc.states.OfficerAuthenticationResponseState
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
class OfficerAuthContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "net.andrc.contracts.OfficerAuthContract"
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
        if (command == Response()) {
            "There can't be no inputs when officer create request" using  (tx.inputs.isNotEmpty())
            val input = tx.inputs.single().state.data as OfficerAuthenticationRequestState
            "Certificate must be valid" using (isValid(input.officerCertificate))
            "Signature must be valid" using (verifySign(input.data, input.signature, input.officerCertificate.publicKey))
            val output = tx.outputs.single().data as OfficerAuthenticationResponseState
            "Certificate must be valid" using (isValid(output.officerCertificate))
            "Signature must be valid" using (verifySign(output.data, output.signature, output.officerCertificate.publicKey))
            "Request and response id must be equal" using (output.requestId == input.requestId)
            "Countries must be equal" using (input.geoData.country == output.geoData.country)
            "Cities must be equal" using (input.geoData.city == output.geoData.city)
        }
    }
}