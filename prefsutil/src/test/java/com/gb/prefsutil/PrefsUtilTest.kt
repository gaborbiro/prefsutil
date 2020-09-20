package com.gb.prefsutil

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    @Mock lateinit var gson: Gson

    private lateinit var underTest: PrefsUtil

    @Before
    fun setUp() {
        underTest = PrefsUtil(context, securePreferences, base64Adapter, gson)
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
        whenever(gson.toJson(mapOf("key1" to "value1"))).thenReturn("serialised")

        underTest.set("key", mapOf("key1" to "value1"))

        verify(securePreferences).put("key", "serialised")
    }

    @Test
    fun setMapInteger() {
        whenever(gson.toJson(mapOf("key1" to 1))).thenReturn("serialised")

        underTest.set("key", mapOf("key1" to 1))

        verify(securePreferences).put("key", "serialised")
    }

    @Test
    fun setList() {
        whenever(gson.toJson(listOf(1, 2, 3))).thenReturn("serialised")

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

        val value: Any? = underTest.get("key")

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
        whenever(gson.fromJson("serialised", Class.forName("java.util.Map")))
            .thenReturn(mapOf("key1" to "value1"))

        val value: Map<String, String>? = underTest.get("key")

        assertThat(value).isEqualTo(mapOf("key1" to "value1"))
    }

    @Test
    fun getNullableList() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("serialised")
        whenever(gson.fromJson("serialised", Class.forName("java.util.List")))
            .thenReturn(listOf(1, 2, 3))

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
        whenever(gson.fromJson("serialised", Class.forName("java.util.Map")))
            .thenReturn(mutableMapOf("key1" to "value1"))

        val value: Map<String, String>? = underTest.get("key")

        assertThat(value).isEqualTo(mutableMapOf("key1" to "value1"))
    }

    @Test
    fun mutableMapIsUpdateable() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("serialised")
        whenever(gson.fromJson<MutableMap<String, String>?>("serialised", object : TypeToken<MutableMap<String, String>?>() {}.type))
            .thenReturn(mutableMapOf("key1" to "value1"))
        whenever(gson.toJson(mutableMapOf("key1" to "value1", "key2" to "value2"))).thenReturn("serialised_new")

        val value: MutableMap<String, String>? = underTest.getMutable("key")
        value!!["key2"] = "value2"

        verify(securePreferences).put("key", "serialised_new")
    }

    @Test
    fun getNullableMutableList() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("serialised")
        whenever(gson.fromJson<MutableList<Int>?>("serialised", object : TypeToken<MutableList<Int>?>() {}.type))
            .thenReturn(mutableListOf(1, 2, 3))

        val value: MutableList<Int>? = underTest.getMutable("key")

        assertThat(listOf(1, 2, 3)).isEqualTo(value)
    }

    @Test
    fun mutableListIsUpdateable() {
        whenever(securePreferences.containsKey("key")).thenReturn(true)
        whenever(securePreferences.getString("key")).thenReturn("serialised")
        whenever(gson.fromJson<MutableList<Int>?>("serialised", object : TypeToken<MutableList<Int>?>() {}.type))
            .thenReturn(mutableListOf(1, 2, 3))
        whenever(gson.toJson(mutableListOf(1, 2, 3, 4))).thenReturn("serialised_new")

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