package com.gb.prefsutil


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.annotations.TestOnly
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class PrefsUtil private constructor(private val appContext: Context, val gson: Gson) {

    private lateinit var securePreferences: SecurePreferencesAdapter
    private lateinit var base64Adapter: Base64Adapter

    constructor(appContext: Context, preferencesName: String, gson: Gson) : this(appContext, gson) {
        securePreferences = SecurePreferences(
            appContext,
            preferencesName,
            generateUDID(appContext),
            true
        ).adapt()
        base64Adapter = Base64Adapter.androidBase64Adapter()
    }

    /**
     * For testing
     */
    @TestOnly
    constructor(
        appContext: Context,
        preferencesAdapter: SecurePreferencesAdapter,
        base64Adapter: Base64Adapter,
        gson: Gson
    ) : this(appContext, gson) {
        securePreferences = preferencesAdapter
        this.base64Adapter = base64Adapter
    }

    fun <T> delegate(key: String, defaultValue: T): PrefDelegate<T> {
        return PrefDelegate(this, key, defaultValue)
    }

    fun delegate(key: String): NullPrefDelegate {
        return NullPrefDelegate(this, key)
    }

    fun <T> mutableDelegate(key: String, defaultValue: T): MutablePrefDelegate<T> {
        return MutablePrefDelegate(this, key, defaultValue)
    }

    fun set(key: String, parcelable: Parcelable) {
        Parcel.obtain().apply {
            parcelable.writeToParcel(this, 0)
            val bytes = marshall()
            recycle()
            set(key, base64Adapter.encodeToString(bytes, 0))
        }
    }

    operator fun <T> get(key: String, creator: Parcelable.Creator<T>, defaultValue: T): T {
        val data = if (containsKey(key)) get(key, "") else return defaultValue

        val bytes = base64Adapter.decode(data, 0)
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        return creator.createFromParcel(parcel)
    }

    fun <T> get(key: String, creator: Parcelable.Creator<T>): T? {
        val data = if (containsKey(key)) get(key, "") else return null

        val bytes = base64Adapter.decode(data, 0)
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        return creator.createFromParcel(parcel)
    }

    operator fun set(key: String, value: Any?) {
        if (value == null) {
            remove(key)
        } else {
            return when (value) {
                is Boolean -> {
                    set(key, java.lang.Boolean.toString(value))
                }
                is Int -> {
                    set(key, value.toString())
                }
                is String -> {
                    set(key, value)
                }
                is Long -> {
                    set(key, value.toString())
                }
                is Float -> {
                    set(key, value.toString())
                }
                is Double -> {
                    set(key, value.toString())
                }
                is Array<*> -> {
                    set(key, value.joinToString(SEPARATOR))
                }
                else -> set(key, gson.toJson(value))
            }
        }
    }

    fun set(key: String, value: String) {
        securePreferences.put(key, value)
    }

    inline operator fun <reified T> get(key: String): T? {
        try {
            return if (containsKey(key)) map(getString(key)) else null
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("${e.message} for $key")
        }
    }

    inline operator fun <reified T> get(key: String, defaultValue: T): T {
        try {
            return if (containsKey(key)) map(getString(key)) else defaultValue
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("${e.message} for $key")
        }
    }

    inline fun <reified T> getMutable(key: String): T? {
        val data = if (containsKey(key)) getString(key) else return null
        return mapMutable(key, data)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    inline fun <reified T> getMutable(key: String, defaultValue: T): T {
        return if (containsKey(key)) mapMutable(key, getString(key)) else {
            when (T::class) {
                Map::class -> {
                    val map = (defaultValue as Map<*, *>)
                    set(key, gson.toJson(map))
                    map.toObservable(key)
                }
                List::class -> {
                    val list = (defaultValue as List<*>)
                    set(key, gson.toJson(defaultValue))
                    list.toObservable(key)
                }
                else -> throw IllegalArgumentException("Unsupported type ${T::class} for $key")
            } as T
        }
    }

    fun getString(key: String): String {
        return securePreferences.getString(key)
            ?: throw IllegalStateException("$key not set in preferences")
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
    @SuppressLint("MissingPermission", "HardwareIds")
    private fun generateUDID(context: Context): String {
        val androidId: String = getHardwareId()
        // androidId changes with every factory reset (which is useful in our case)
        val udid = if ("9774d56d682e549c" != androidId) {
            java.util.UUID.nameUUIDFromBytes(androidId.toByteArray(charset("utf8")))
        } else {
            // On some 2.2 devices androidId is always 9774d56d682e549c, which is unsafe
            val deviceId = appContext.getSystemService<TelephonyManager>()?.deviceId
            if (deviceId.isNullOrBlank()) {
                java.util.UUID.randomUUID()
            } else {
                java.util.UUID.nameUUIDFromBytes(deviceId.toByteArray(charset("utf8")))
            }
        }.toString()
        // TODO remove after the prefs reset bug is sorted out
        context.getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE).apply {
            if (this.getString("udid", null) != udid) {
                Toast.makeText(context, "PrefsUtils UDID changed to $udid", Toast.LENGTH_LONG)
                    .show()
                edit {
                    putString("udid", udid)
                }
            }
        }
        return udid
    }

    @SuppressLint("HardwareIds")
    private fun getHardwareId() = android.provider.Settings.Secure.getString(
        appContext.contentResolver,
        android.provider.Settings.Secure.ANDROID_ID
    )

    inline fun <reified T> map(data: String) = mapType(data, T::class) as T

    fun mapType(data: String, type: KClass<*>): Any {
        return if (type.qualifiedName == Array<Any>::class.qualifiedName) {
            mapArray(data, type)
        } else {
            mapOther(data, type)
        }
    }

    fun mapArray(data: String, type: KClass<*>): Any {
        val itemType = type.java.componentType.kotlin
        val list = data.split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.map {
            mapType(it, itemType)
        }
        // TODO use kotlin reflection to create array and use toArray instead of toTypedArray?
        return when (itemType) {
            Boolean::class -> {
                (list as List<Boolean>).toTypedArray()
            }
            Int::class -> {
                (list as List<Int>).toTypedArray()
            }
            String::class -> {
                (list as List<String>).toTypedArray()
            }
            Long::class -> {
                (list as List<Long>).toTypedArray()
            }
            Float::class -> {
                (list as List<Float>).toTypedArray()
            }
            Double::class -> {
                (list as List<Double>).toTypedArray()
            }
            Map::class -> {
                (list as List<Map<*, *>>).toTypedArray()
            }
            List::class -> {
                (list as List<List<*>>).toTypedArray()
            }
            else -> throw IllegalArgumentException("Unsupported type $type")
        }
    }

    protected fun mapOther(data: String, type: KClass<*>): Any {
        return when (type) {
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
            Double::class -> {
                java.lang.Double.valueOf(data)
            }
            Map::class -> {
                gson.fromJson(data, type.java)
            }
            List::class -> {
                gson.fromJson(data, type.java)
            }
            else -> throw IllegalArgumentException("Unsupported type $type")
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    inline fun <reified T> mapMutable(key: String, data: String): T {
        return when (T::class) {
            Map::class -> {
                (gson.fromJson<T>(data, object : TypeToken<T>() {}.type) as Map<*, *>).toObservable(
                    key
                )
            }
            List::class -> {
                (gson.fromJson<T>(data, object : TypeToken<T>() {}.type) as List<*>).toObservable(
                    key
                )
            }
            else -> throw IllegalArgumentException("Unsupported type ${T::class} for $key")
        } as T
    }

    fun Map<*, *>.toObservable(key: String) = ObservableMap(this.toMutableMap()) {
        set(key, it)
    }

    fun List<*>.toObservable(key: String) = ObservableList(this.toMutableList()) {
        set(key, it)
    }
}

const val SEPARATOR = "dfg,hsdfk__jg34n95t"

class PrefDelegate<T>(val prefsUtil: PrefsUtil, val key: String, val defaultValue: T) {
    operator fun setValue(parent: Any, property: KProperty<*>, value: Any) {
        prefsUtil.set(key, value)
    }

    inline operator fun <reified T2> getValue(parent: Any, property: KProperty<*>): T2 =
        prefsUtil.get(key, defaultValue as T2)
}

class MutablePrefDelegate<T>(val prefsUtil: PrefsUtil, val key: String, val defaultValue: T) {
    operator fun setValue(parent: Any, property: KProperty<*>, value: Any?) {
        prefsUtil.set(key, value)
    }

    inline operator fun <reified T> getValue(parent: Any, property: KProperty<*>): T =
        prefsUtil.getMutable(key, defaultValue as T)
}

class NullPrefDelegate(val prefsUtil: PrefsUtil, val key: String) {

    operator fun setValue(parent: Any, property: KProperty<*>, value: Any?) {
        prefsUtil.set(key, value)
    }

    inline operator fun <reified T> getValue(parent: Any, property: KProperty<*>): T? =
        prefsUtil.get(key) as T?
}