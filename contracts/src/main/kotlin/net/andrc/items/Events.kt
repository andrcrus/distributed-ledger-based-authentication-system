package net.andrc.items

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class Events {
    Shaking,
    StrongShaking,
    StopShaking,
    StopStrongShaking,
    TurnOffTheFrige,
    TurnOnTheFrige
}