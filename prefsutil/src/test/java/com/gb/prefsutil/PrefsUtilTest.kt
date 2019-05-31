package com.gb.prefsutil

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit

class PrefsUtilTest {
    @JvmField @Rule val mockitoRule = MockitoJUnit.rule()!!

    @Mock lateinit var context: Context
    @Mock lateinit var securePreferences: SecurePreferencesAdapter
    @Mock lateinit var base64Adapter: Base64Adapter

    private lateinit var underTest: PrefsUtil

    @Before
    fun setUp() {
        underTest = PrefsUtil(context, securePreferences, base64Adapter)
    }

    @Test
    fun setBoolean() {
        underTest["key"] = true

        verify(securePreferences).put("key", "true")
    }

    @Test
    fun setInt() {
        underTest["key"] = 1

        verify(securePreferences).put("key", "1")
    }

    @Test
    fun setNullableString() {
        val str: String? = "value"
        underTest["key"] = str

        verify(securePreferences).put("key", "value")
    }

    @Test
    fun setLong() {
        underTest["key"] = 1L

        verify(securePreferences).put("key", "1")
    }

    @Test
    fun setFloat() {
        underTest["key"] = 1.234f

        verify(securePreferences).put("key", "1.234")
    }

    @Test
    fun setDouble() {
        underTest["key"] = 1.234

        verify(securePreferences).put("key", "1.234")
    }


    @Test
    fun setArray() {
        underTest.set("key", arrayOf(1, 2, 3))

        verify(securePreferences).put("key", "1${SEPARATOR}2${SEPARATOR}3")
    }

    @Test
    fun setMapString() {
        val byteArray = ByteArray(SERIALISED_MAP_KEY1_VALUE1.size) { i -> SERIALISED_MAP_KEY1_VALUE1[i].toByte() }
        whenever(base64Adapter.encodeToString(byteArray, 0)).thenReturn("serialised")

        underTest.set("key", mapOf("key1" to "value1"))

        verify(securePreferences).put("key", "serialised")
    }

    @Test
    fun setMapInteger() {
        val byteArray = ByteArray(SERIALISED_MAP_KEY1_1.size) { i -> SERIALISED_MAP_KEY1_1[i].toByte() }
        whenever(base64Adapter.encodeToString(byteArray, 0)).thenReturn("serialised")

        underTest.set("key", mapOf("key1" to 1))

        verify(securePreferences).put("key", "serialised")
    }

    @Test
    fun setList() {
        val byteArray = ByteArray(SERIALISED_LIST_1_2_3.size) { i -> SERIALISED_LIST_1_2_3[i].toByte() }
        whenever(base64Adapter.encodeToString(byteArray, 0)).thenReturn("serialised")

        underTest.set("key", listOf(1, 2, 3))

        verify(securePreferences).put("key", "serialised")
    }

    @Test
    fun setNull() {
        val str: String? = null

        underTest.set("key", str)

        verify(securePreferences).removeValue("key")
    }

    @Test
    fun setString() {
        underTest.set("key", "value")

        verify(securePreferences).put("key", "value")
    }

    @Test
    fun getNullableObjectMissing() {
        whenever(securePreferences.containsKey("key")).thenReturn(false)

        val value: String? = underTest.get("key")

        assertThat(value).isNull()
    }

    @Test
    fun getNullableBoolean() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("true")

        val value: Boolean? = underTest.get("key")

        assertThat(value).isEqualTo(true)
    }

    @Test
    fun getNullableInt() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("1")

        val value: Int? = underTest.get("key")

        assertThat(value).isEqualTo(1)
    }

    @Test
    fun getNullableString() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("value")

        val value: String? = underTest.get("key")

        assertThat(value).isEqualTo("value")
    }

    @Test
    fun getNullableLong() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("1")

        val value: Long? = underTest.get("key")

        assertThat(value).isEqualTo(1L)
    }

    @Test
    fun getNullableFloat() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("1.234")

        val value: Float? = underTest.get("key")

        assertThat(value).isEqualTo(1.234f)
    }

    @Test
    fun getNullableDouble() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("1.234")

        val value: Double? = underTest.get("key")

        assertThat(value).isEqualTo(1.234)
    }

    @Test
    fun getNullableArray() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("1${SEPARATOR}2${SEPARATOR}3")

        val value: Array<Int>? = underTest.get("key")

        assertThat(value).isEqualTo(arrayOf(1, 2, 3))
    }

    @Test
    fun getNullableMap() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("serialised")
        val byteArray = ByteArray(SERIALISED_MAP_KEY1_VALUE1.size) { i -> SERIALISED_MAP_KEY1_VALUE1[i].toByte() }
        whenever(
            base64Adapter.decode(
                "serialised",
                0
            )
        ).thenReturn(byteArray)

        val value: Map<String, String>? = underTest["key"]

        assertThat(value).isEqualTo(mapOf("key1" to "value1"))
    }

    @Test
    fun getNullableList() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("serialised")
        val byteArray = ByteArray(SERIALISED_LIST_1_2_3.size) { i -> SERIALISED_LIST_1_2_3[i].toByte() }
        whenever(
            base64Adapter.decode(
                "serialised",
                0
            )
        ).thenReturn(byteArray)

        val value: List<Int>? = underTest.get("key")

        assertThat(listOf(1, 2, 3)).isEqualTo(value)
    }

    @Test
    fun getDefault() {
        whenever(securePreferences.containsKey("key")).thenReturn(false)

        val value: String = underTest.get("key", "default")

        assertThat(value).isEqualTo("default")
    }

    @Test
    fun getNullableMutableMap() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("serialised")
        val byteArray = ByteArray(SERIALISED_MAP_KEY1_VALUE1.size) { i -> SERIALISED_MAP_KEY1_VALUE1[i].toByte() }
        whenever(
            base64Adapter.decode(
                "serialised",
                0
            )
        ).thenReturn(byteArray)

        val value: MutableMap<String, String>? = underTest.getMutable("key")

        assertThat(mapOf("key1" to "value1")).isEqualTo(value)
    }

    @Test
    fun mutableMapIsUpdateable() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("serialised")
        val byteArray = ByteArray(SERIALISED_MAP_KEY1_VALUE1.size) { i -> SERIALISED_MAP_KEY1_VALUE1[i].toByte() }
        whenever(
            base64Adapter.decode(
                "serialised",
                0
            )
        ).thenReturn(byteArray)
        val byteArrayNew =
            ByteArray(SERIALISED_MAP_KEY1_VALUE1_KEY2_VALUE2.size) { i -> SERIALISED_MAP_KEY1_VALUE1_KEY2_VALUE2[i].toByte() }
        whenever(base64Adapter.encodeToString(byteArrayNew, 0)).thenReturn("serialised_new")

        val value: MutableMap<String, String>? = underTest.getMutable("key")
        value!!["key2"] = "value2"

        verify(securePreferences).put("key", "serialised_new")
    }

    @Test
    fun getNullableMutableList() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("serialised")
        val byteArray = ByteArray(SERIALISED_LIST_1_2_3.size) { i -> SERIALISED_LIST_1_2_3[i].toByte() }
        whenever(
            base64Adapter.decode(
                "serialised",
                0
            )
        ).thenReturn(byteArray)

        val value: MutableList<Int>? = underTest.getMutable("key")

        assertThat(listOf(1, 2, 3)).isEqualTo(value)
    }

    @Test
    fun mutableListIsUpdateable() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("serialised")
        val byteArray = ByteArray(SERIALISED_LIST_1_2_3.size) { i -> SERIALISED_LIST_1_2_3[i].toByte() }
        whenever(
            base64Adapter.decode(
                "serialised",
                0
            )
        ).thenReturn(byteArray)
        val byteArrayNew = ByteArray(SERIALISED_LIST_1_2_3_4.size) { i -> SERIALISED_LIST_1_2_3_4[i].toByte() }
        whenever(base64Adapter.encodeToString(byteArrayNew, 0)).thenReturn("serialised_new")

        val value: MutableList<Int>? = underTest.getMutable("key")
        value!!.add(4)

        verify(securePreferences).put("key", "serialised_new")
    }

    @Test
    fun getString() {
    }

    @Test
    fun containsKey() {
    }

    @Test
    fun remove() {
    }

    @Test
    fun clear() {
    }
}

/**
 * Equivalent to mapOf("key1" to "value1") serialised
 */
val SERIALISED_MAP_KEY1_VALUE1 = arrayOf(
    -84, -19, 0, 5, 115, 114, 0, 34, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 67, 111, 108, 108, 101, 99, 116, 105, 111, 110, 115, 36, 83, 105, 110, 103, 108, 101, 116, 111, 110, 77, 97, 112, -97, 35, 9, -111, 113, 127, 107, -111, 2, 0, 2, 76, 0, 1, 107, 116, 0, 18, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 76, 0, 1, 118, 113, 0, 126, 0, 1, 120, 112, 116, 0, 4, 107, 101, 121, 49, 116, 0, 6, 118, 97, 108, 117, 101, 49
)

val SERIALISED_MAP_KEY1_VALUE1_KEY2_VALUE2 = arrayOf(
    -84, -19, 0, 5, 115, 114, 0, 23, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 76, 105, 110, 107, 101, 100, 72, 97, 115, 104, 77, 97, 112, 52, -64, 78, 92, 16, 108, -64, -5, 2, 0, 1, 90, 0, 11, 97, 99, 99, 101, 115, 115, 79, 114, 100, 101, 114, 120, 114, 0, 17, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 72, 97, 115, 104, 77, 97, 112, 5, 7, -38, -63, -61, 22, 96, -47, 3, 0, 2, 70, 0, 10, 108, 111, 97, 100, 70, 97, 99, 116, 111, 114, 73, 0, 9, 116, 104, 114, 101, 115, 104, 111, 108, 100, 120, 112, 63, 64, 0, 0, 0, 0, 0, 3, 119, 8, 0, 0, 0, 4, 0, 0, 0, 2, 116, 0, 4, 107, 101, 121, 49, 116, 0, 6, 118, 97, 108, 117, 101, 49, 116, 0, 4, 107, 101, 121, 50, 116, 0, 6, 118, 97, 108, 117, 101, 50, 120, 0
)

val SERIALISED_MAP_KEY1_1 = arrayOf(
    -84, -19, 0, 5, 115, 114, 0, 34, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 67, 111, 108, 108, 101, 99, 116, 105, 111, 110, 115, 36, 83, 105, 110, 103, 108, 101, 116, 111, 110, 77, 97, 112, -97, 35, 9, -111, 113, 127, 107, -111, 2, 0, 2, 76, 0, 1, 107, 116, 0, 18, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 76, 0, 1, 118, 113, 0, 126, 0, 1, 120, 112, 116, 0, 4, 107, 101, 121, 49, 115, 114, 0, 17, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 73, 110, 116, 101, 103, 101, 114, 18, -30, -96, -92, -9, -127, -121, 56, 2, 0, 1, 73, 0, 5, 118, 97, 108, 117, 101, 120, 114, 0, 16, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 78, 117, 109, 98, 101, 114, -122, -84, -107, 29, 11, -108, -32, -117, 2, 0, 0, 120, 112, 0, 0, 0, 1
)

val SERIALISED_LIST_1_2_3 = arrayOf(
    -84, -19, 0, 5, 115, 114, 0, 26, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 65, 114, 114, 97, 121, 115, 36, 65, 114, 114, 97, 121, 76, 105, 115, 116, -39, -92, 60, -66, -51, -120, 6, -46, 2, 0, 1, 91, 0, 1, 97, 116, 0, 19, 91, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 120, 112, 117, 114, 0, 20, 91, 76, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 73, 110, 116, 101, 103, 101, 114, 59, -2, -105, -83, -96, 1, -125, -30, 27, 2, 0, 0, 120, 112, 0, 0, 0, 3, 115, 114, 0, 17, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 73, 110, 116, 101, 103, 101, 114, 18, -30, -96, -92, -9, -127, -121, 56, 2, 0, 1, 73, 0, 5, 118, 97, 108, 117, 101, 120, 114, 0, 16, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 78, 117, 109, 98, 101, 114, -122, -84, -107, 29, 11, -108, -32, -117, 2, 0, 0, 120, 112, 0, 0, 0, 1, 115, 113, 0, 126, 0, 5, 0, 0, 0, 2, 115, 113, 0, 126, 0, 5, 0, 0, 0, 3
)

val SERIALISED_LIST_1_2_3_4 = arrayOf(
    -84, -19, 0, 5, 115, 114, 0, 19, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 65, 114, 114, 97, 121, 76, 105, 115, 116, 120, -127, -46, 29, -103, -57, 97, -99, 3, 0, 1, 73, 0, 4, 115, 105, 122, 101, 120, 112, 0, 0, 0, 4, 119, 4, 0, 0, 0, 4, 115, 114, 0, 17, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 73, 110, 116, 101, 103, 101, 114, 18, -30, -96, -92, -9, -127, -121, 56, 2, 0, 1, 73, 0, 5, 118, 97, 108, 117, 101, 120, 114, 0, 16, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 78, 117, 109, 98, 101, 114, -122, -84, -107, 29, 11, -108, -32, -117, 2, 0, 0, 120, 112, 0, 0, 0, 1, 115, 113, 0, 126, 0, 2, 0, 0, 0, 2, 115, 113, 0, 126, 0, 2, 0, 0, 0, 3, 115, 113, 0, 126, 0, 2, 0, 0, 0, 4, 120
)