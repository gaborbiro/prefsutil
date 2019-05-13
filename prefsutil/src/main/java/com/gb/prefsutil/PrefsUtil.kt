package com.gb.prefsutil


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import android.telephony.TelephonyManager
import android.util.Base64
import androidx.core.content.getSystemService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.reflect.KProperty

class PrefsUtil constructor(private val appContext: Context, private val preferencesName: String) {

    private val securePreferences: SecurePreferences by lazy {
        SecurePreferences(
            appContext,
            preferencesName,
            generateUDID(),
            true
        )
    }

    fun <T> delegate(key: String, defaultValue: T): PrefDelegate<T> {
        return PrefDelegate(this, key, defaultValue)
    }

    fun delegate(key: String): NullPrefDelegate {
        return NullPrefDelegate(this, key)
    }

    fun set(key: String, parcelable: Parcelable) {
        Parcel.obtain().apply {
            parcelable.writeToParcel(this, 0)
            val bytes = marshall()
            recycle()
            set(key, Base64.encodeToString(bytes, 0))
        }
    }

    operator fun <T> get(key: String, creator: Parcelable.Creator<T>, defaultValue: T): T {
        val data = if (containsKey(key)) get(key, "") else return defaultValue

        val bytes = Base64.decode(data, 0)
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        return creator.createFromParcel(parcel)
    }

    fun <T> get(key: String, creator: Parcelable.Creator<T>): T? {
        val data = if (containsKey(key)) get(key, "") else return null

        val bytes = Base64.decode(data, 0)
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        return creator.createFromParcel(parcel)
    }

    inline operator fun <reified T> set(key: String, value: T?) {
        if (value == null) {
            remove(key)
        } else {
            return when (value) {
                is Boolean -> {
                    set(key, java.lang.Boolean.toString(value))
                }
                is Int -> {
                    set(key, Integer.toString(value as Int))
                }
                is String -> {
                    set(key, value as String)
                }
                is Long -> {
                    set(key, java.lang.Long.toString(value as Long))
                }
                is Float -> {
                    set(key, java.lang.Float.toString(value as Float))
                }
                is Map<*, *> -> {
                    val baos = ByteArrayOutputStream()
                    val oos = ObjectOutputStream(baos)
                    try {
                        oos.writeObject(value)
                        oos.flush()
                        set(key, Base64.encodeToString(baos.toByteArray(), 0))
                    } finally {
                        try {
                            oos.close()
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
                is Array<*> -> {
                    set(key, (value as Array<*>).joinToString(SEPARATOR))
                }
                else -> throw IllegalArgumentException("Unsupported type ${T::class} for $key")
            }
        }
    }

    fun set(key: String, value: String) {
        securePreferences.put(key, value)
    }

    inline operator fun <reified T> get(key: String): T? {
        val data = if (containsKey(key)) get(key, "") else return null
        if (data.isBlank()) return null

        return when (T::class) {
            Boolean::class -> {
                java.lang.Boolean.valueOf(data)
            }
            Int::class -> {
                Integer.valueOf(data)
            }
            String::class -> {
                data
            }
            Long::class -> {
                java.lang.Long.valueOf(data)
            }
            Float::class -> {
                java.lang.Float.valueOf(data)
            }
            MutableMap::class -> {
                val bytes = Base64.decode(data, 0)
                val ois = ObjectInputStream(ByteArrayInputStream(bytes, 0, bytes.size))
                ObservableMap(ois.readObject() as MutableMap<*, *>) {
                    set(key, it)
                }
            }
            Map::class -> {
                val bytes = Base64.decode(data, 0)
                val ois = ObjectInputStream(ByteArrayInputStream(bytes, 0, bytes.size))
                ois.readObject()
            }
            is Array<*> -> {
                data.split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }
            else -> throw IllegalArgumentException("Unsupported type ${T::class} for $key")
        } as T?
    }

    inline operator fun <reified T> get(key: String, defaultValue: T): T {
        val data = if (containsKey(key)) get(key, "") else ""

        return when (T::class) {
            Boolean::class -> {
                if (data.isBlank()) {
                    defaultValue
                } else {
                    java.lang.Boolean.valueOf(data)
                }
            }
            Int::class -> {
                if (data.isBlank()) {
                    defaultValue
                } else {
                    Integer.valueOf(data)
                }
            }
            String::class -> {
                if (data.isBlank()) {
                    defaultValue
                } else {
                    data
                }
            }
            Long::class -> {
                if (data.isBlank()) {
                    defaultValue
                } else {
                    java.lang.Long.valueOf(data)
                }
            }
            Float::class -> {
                if (data.isBlank()) {
                    defaultValue
                } else {
                    java.lang.Float.valueOf(data)
                }
            }
            MutableMap::class -> {
                if (data.isBlank()) {
                    set(key, defaultValue)
                    ObservableMap(defaultValue as MutableMap<*, *>) {
                        set(key, it)
                    }
                } else {
                    val bytes = Base64.decode(data, 0)
                    val ois = ObjectInputStream(ByteArrayInputStream(bytes, 0, bytes.size))
                    ObservableMap(ois.readObject() as MutableMap<*, *>) {
                        set(key, it)
                    }
                }
            }
            Map::class -> {
                if (data.isBlank()) {
                    defaultValue
                } else {
                    val bytes = Base64.decode(data, 0)
                    val ois = ObjectInputStream(ByteArrayInputStream(bytes, 0, bytes.size))
                    ois.readObject()
                }
            }
            is Array<*> -> {
                if (data.isBlank()) {
                    defaultValue
                } else {
                    data.split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                }
            }
            else -> throw IllegalArgumentException("Unsupported type ${T::class} for $key")
        } as T
    }

    operator fun get(key: String, defaultValue: String): String {
        return securePreferences.getString(key) ?: defaultValue
    }

    fun containsKey(key: String): Boolean = securePreferences.containsKey(key)

    fun remove(key: String) {
        securePreferences.removeValue(key)
    }

    fun clear() {
        securePreferences.clear()
    }

    /**
     * Registers a callback to be invoked when a change happens to the specified preference.
     *
     * @param key      Preference key for which the specified callback should be
     * registered to
     * @param listener The callback that will run.
     * @see .unregisterOnSharedPreferenceChangeListener
     */
    fun registerOnSharedPreferenceChangeListener(
        key: String,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        securePreferences.registerOnSharedPreferenceChangeListener(key, listener)
    }

    /**
     * Unregisters a previous callback.
     *
     * @param key PReference key, the callback of which that should be unregistered.
     * @see .registerOnSharedPreferenceChangeListener
     */
    fun unregisterOnSharedPreferenceChangeListener(key: String) {
        securePreferences.unregisterOnSharedPreferenceChangeListener(key)
    }

    /**
     * Generate a unique id for the device. Changes with every factory reset. If the device doesn't have a proper
     * android_id and deviceId, it falls back to a randomly generated id, that is persisted in SharedPreferences.
     */
    @SuppressLint("MissingPermission")
    private fun generateUDID(): String {
        val androidId: String = getHardwareId()
        // androidId changes with every factory reset (which is useful in our case)
        return if ("9774d56d682e549c" != androidId) {
            UUID.nameUUIDFromBytes(androidId.toByteArray(charset("utf8")))
        } else {
            // On some 2.2 devices androidId is always 9774d56d682e549c, which is unsafe
            val deviceId = appContext.getSystemService<TelephonyManager>()?.deviceId
            if (deviceId.isNullOrBlank()) {
                UUID.randomUUID()
            } else {
                UUID.nameUUIDFromBytes(deviceId.toByteArray(charset("utf8")))
            }
        }.toString()
    }

    @SuppressLint("HardwareIds")
    private fun getHardwareId() = android.provider.Settings.Secure.getString(
        appContext.contentResolver,
        android.provider.Settings.Secure.ANDROID_ID
    )
}

const val SEPARATOR = "dfg,hsdfk__jg34n95t"

class PrefDelegate<T>(val prefsUtil: PrefsUtil, val key: String, val defaultValue: T) {
    operator fun setValue(parent: Any, property: KProperty<*>, value: Any) {
        prefsUtil[key] = value
    }

    inline operator fun <reified T2> getValue(parent: Any, property: KProperty<*>): T2 =
        prefsUtil[key, defaultValue as T2]
}

class NullPrefDelegate(val prefsUtil: PrefsUtil, val key: String) {
    operator fun setValue(parent: Any, property: KProperty<*>, value: Any?) {
        prefsUtil[key] = value
    }

    inline operator fun <reified T> getValue(parent: Any, property: KProperty<*>): T? =
        prefsUtil[key]
}