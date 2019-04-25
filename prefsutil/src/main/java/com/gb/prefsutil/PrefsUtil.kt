package com.gb.prefsutil


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import android.telephony.TelephonyManager
import android.util.Base64
import androidx.core.content.getSystemService
import java.io.*
import java.util.*

class PrefsUtil constructor(private val appContext: Context, private val preferencesName: String) {

    private val securePreferences: SecurePreferences by lazy {
        SecurePreferences(
            appContext,
            preferencesName,
            generateUDID(),
            true
        )
    }

    fun put(key: String, parcelable: Parcelable) {
        Parcel.obtain().apply {
            parcelable.writeToParcel(this, 0)
            val bytes = marshall()
            recycle()
            put(key, Base64.encodeToString(bytes, 0))
        }
    }

    operator fun <T> get(key: String, creator: Parcelable.Creator<T>): T? {
        val data = get(key, null as String?) ?: return null

        val bytes = Base64.decode(data, 0)
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        return creator.createFromParcel(parcel)
    }

    operator fun get(key: String, defaultValues: Map<*, *>): Map<*, *>? {
        val data = get(key, null as String?) ?: return defaultValues

        try {
            val bytes = Base64.decode(data, 0)
            val ois = ObjectInputStream(ByteArrayInputStream(bytes, 0, bytes.size))
            return ois.readObject() as Map<*, *>
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    fun put(key: String, map: Map<*, *>) {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        try {
            oos.writeObject(map)
            oos.flush()
            put(key, Base64.encodeToString(baos.toByteArray(), 0))
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                oos.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    operator fun get(key: String, defaultValues: Array<String>?): Array<String?> {
        val defaultValuesStr: String?
        if (defaultValues == null) {
            defaultValuesStr = null
        } else {
            defaultValuesStr = defaultValues.joinToString(SEPARATOR)
        }
        val text = get(key, defaultValuesStr)
        return if (!text.isNullOrEmpty()) {
            text.split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else {
            arrayOfNulls(0)
        }
    }

    fun put(key: String, values: Array<String>) {
        put(key, values.joinToString(SEPARATOR))
    }

    fun put(key: String, value: Boolean) {
        securePreferences.put(key, java.lang.Boolean.toString(value))
    }

    operator fun get(key: String, defaultValue: Boolean): Boolean {
        val value = securePreferences.getString(key)
        return if (value.isNullOrEmpty()) defaultValue else java.lang.Boolean.valueOf(value)
    }

    fun put(key: String, value: String?) {
        value?.let { securePreferences.put(key, value) } ?: run { securePreferences.removeValue(key) }
    }

    operator fun get(key: String, defaultValue: String?): String? {
        return securePreferences.getString(key) ?: defaultValue
    }

    fun put(key: String, value: Int) {
        securePreferences.put(key, Integer.toString(value))
    }

    operator fun get(key: String, defaultValue: Int): Int {
        val value = securePreferences.getString(key)
        return if (value.isNullOrEmpty()) defaultValue else Integer.valueOf(value)
    }

    fun put(key: String, value: Long) {
        securePreferences.put(key, java.lang.Long.toString(value))
    }

    operator fun get(key: String, defaultValue: Long): Long {
        val value = securePreferences.getString(key)
        return if (value.isNullOrEmpty()) defaultValue else java.lang.Long.valueOf(value)
    }

    fun put(key: String, value: Float) {
        securePreferences.put(key, java.lang.Float.toString(value))
    }

    operator fun get(key: String, defaultValue: Float): Float {
        val value = securePreferences.getString(key)
        return if (value.isNullOrEmpty()) defaultValue else java.lang.Float.valueOf(value)
    }

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

private const val SEPARATOR = "dfg,hsdfk__jg34n95t"
