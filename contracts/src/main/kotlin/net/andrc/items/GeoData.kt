package net.andrc.items

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class GeoData(
        val city: String,
        val country: String,
        val lat: Double,
        val lon: Double
) {
    override fun toString(): String {
        return """ {
            |    "city" : "$city",
            |    "country" : "$country",
            |    "lat" : $lat,
            |    "lon" : $lon
            |}
            |
        """.trimMargin()
    }
}