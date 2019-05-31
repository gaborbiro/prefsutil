package com.gb.prefsutil

import android.content.SharedPreferences

interface SecurePreferencesAdapter {
    fun put(key: String, value: String?)
    fun getString(key: String): String?
    fun containsKey(key: String): Boolean
    fun removeValue(key: String)
    fun clear()
    fun registerOnSharedPreferenceChangeListener(
        key: String,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    )
    fun unregisterOnSharedPreferenceChangeListener(key: String)
}

internal fun SecurePreferences.adapt() = object : SecurePreferencesAdapter {
    override fun put(key: String, value: String?) {
        this@adapt.put(key, value)
    }

    override fun getString(key: String) = this@adapt.getString(key)

    override fun containsKey(key: String) = this@adapt.containsKey(key)

    override fun removeValue(key: String) {
        this@adapt.removeValue(key)
    }

    override fun clear() {
        this@adapt.clear()
    }

    override fun registerOnSharedPreferenceChangeListener(
        key: String,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        this@adapt.registerOnSharedPreferenceChangeListener(key, listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(key: String) {
        this@adapt.unregisterOnSharedPreferenceChangeListener(key)
    }
}