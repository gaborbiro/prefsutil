package com.gb.prefsutil

interface Serializable {
    fun serialize(): String
    fun deserialize(serialised: String): Any
}