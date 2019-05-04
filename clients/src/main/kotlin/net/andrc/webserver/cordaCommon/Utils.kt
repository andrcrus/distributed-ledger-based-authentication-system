package net.andrc.webserver.cordaCommon

import net.andrc.states.PutContainerState
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.SignedTransaction


fun SignedTransaction.toJson(): String {
    return """{
        |"tx" : "${this.tx}",
        |"id" : "${this.id}",
        |"sigs: "${this.sigs}"
        |}""".trimMargin()
}

fun List<StateAndRef<PutContainerState>>.toJson(): String {
    val builder = StringBuilder("[")
    val itt = iterator()
    while (itt.hasNext()) {
        builder.append(itt.next().state.data.toString())
        if (itt.hasNext()) {
            builder.append(",\n")
        }
    }
    builder.append("]")
    return builder.toString()
}