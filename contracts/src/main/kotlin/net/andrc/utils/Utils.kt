package net.andrc.utils

import java.lang.StringBuilder

fun List<String>.toJsonString(): String {
    val result = StringBuilder("[")
    for (i in 0..size) {
        if (i != size-1 ) {
            result.append(",")
        }
    }
    result.append("]")
    return result.toString()
}