package com.gb.prefsutil

import android.util.Base64

interface Base64Adapter {
    fun encodeToString(input: ByteArray, flags: Int): String
    fun decode(str: String, flags: Int): ByteArray

    companion object {
        fun androidBase64Adapter() = object : Base64Adapter {
            override fun encodeToString(input: ByteArray, flags: Int) = Base64.encodeToString(input, flags)

            override fun decode(str: String, flags: Int) = Base64.decode(str, flags)
        }
    }
}