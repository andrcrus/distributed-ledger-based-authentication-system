package net.andrc.webserver.cordaCommon

import net.corda.core.transactions.SignedTransaction


fun SignedTransaction.toJson(): String {
    return """{
        |"tx" : "${this.tx}",
        |"id" : "${this.id}",
        |"sigs: "${this.sigs}"
        |}""".trimMargin()
}