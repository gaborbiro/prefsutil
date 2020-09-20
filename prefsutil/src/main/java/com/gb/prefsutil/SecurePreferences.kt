package com.gb.prefsutil

/*
 Copyright (C) 2012 Sveinung Kval Bakken, sveinung.bakken@gmail.com

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal class SecurePreferences
/**
 * This will initialize an instance of the SecurePreferences class
 *
 * @param context        your current context.
 * @param preferenceName name of preferences file (preferenceName.xml)
 * @param secureKey      the key used for encryption, finding a good key scheme is
 * hard. Hardcoding your key in the application
 * is bad, but better than plaintext preferences. Having the
 * user enter the key upon application launch
 * is a safe(r) alternative, but annoying to the user.
 * @param encryptKeys    settings this to false will only encrypt the values, true
 * will encrypt both values and keys. Keys can
 * contain a lot of information about the plaintext value of
 * the value which can be used to decipher the
 * value.
 * @throws SecurePreferencesException
 */
@Throws(SecurePreferencesException::class)
constructor(
    context: Context,
    preferenceName: String,
    secureKey: String,
    encryptKeys: Boolean
) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val encryptKeys: Boolean
    private val writer: Cipher
    private val reader: Cipher
    private val keyWriter: Cipher
    private val keyReader: Cipher
    private val preferences: SharedPreferences

    private val listeners: MutableMap<String, SharedPreferences.OnSharedPreferenceChangeListener> = mutableMapOf()

    private val iv: IvParameterSpec
        get() {
            val iv = ByteArray(writer.blockSize)
            System.arraycopy("fldsjfodasjifudslfjdsaofshaufihadsf".toByteArray(), 0, iv, 0, writer.blockSize)
            return IvParameterSpec(iv)
        }

    init {
        try {
            this.writer = Cipher.getInstance(TRANSFORMATION)
            this.reader = Cipher.getInstance(TRANSFORMATION)
            this.keyWriter = Cipher.getInstance(KEY_TRANSFORMATION)
            this.keyReader = Cipher.getInstance(KEY_TRANSFORMATION)

            initCiphers(secureKey)

            this.preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            this.preferences.registerOnSharedPreferenceChangeListener(this)
            this.encryptKeys = encryptKeys
        } catch (e: GeneralSecurityException) {
            throw SecurePreferencesException(e)
        } catch (e: UnsupportedEncodingException) {
            throw SecurePreferencesException(e)
        }
    }

    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun initCiphers(secureKey: String) {
        val ivSpec = iv
        val secretKey = getSecretKey(secureKey)

        writer.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        reader.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        keyWriter.init(Cipher.ENCRYPT_MODE, secretKey)
        keyReader.init(Cipher.DECRYPT_MODE, secretKey)
    }

    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class)
    private fun getSecretKey(key: String): SecretKeySpec {
        val keyBytes = createKeyBytes(key)
        return SecretKeySpec(keyBytes, TRANSFORMATION)
    }

    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class)
    private fun createKeyBytes(key: String): ByteArray {
        return MessageDigest.getInstance(SECRET_KEY_HASH_TRANSFORMATION).apply {
            reset()
        }.digest(key.toByteArray(charset(CHARSET)))
    }

    private fun toKey(key: String) = if (encryptKeys) encrypt(key, keyWriter) else key

    @Throws(SecurePreferencesException::class)
    private fun putValue(key: String, value: String) {
        val secureValueEncoded = encrypt(value, writer)
        preferences.edit {
            putString(key, secureValueEncoded)
        }
    }

    @Throws(SecurePreferencesException::class)
    private fun encrypt(value: String, writer: Cipher): String {
        val secureValue: ByteArray
        try {
            secureValue = convert(
                writer,
                value.toByteArray(charset(CHARSET))
            )
        } catch (e: UnsupportedEncodingException) {
            throw SecurePreferencesException(e)
        }

        return Base64.encodeToString(secureValue, Base64.NO_WRAP)
    }

    private fun decryptValue(securedEncodedValue: String?): String {
        return decrypt(securedEncodedValue, reader)
    }

    private fun decryptKey(securedEncodedKey: String): String {
        return decrypt(securedEncodedKey, keyReader)
    }

    private fun decrypt(securedEncodedValue: String?, cipher: Cipher): String {
        val securedValue = Base64.decode(securedEncodedValue, Base64.NO_WRAP)
        val value = convert(cipher, securedValue)
        try {
            return String(value, Charset.forName(CHARSET))
        } catch (e: UnsupportedEncodingException) {
            throw SecurePreferencesException(e)
        }
    }

    fun put(key: String, value: String?) {
        if (value == null) {
            removeValue(key)
        } else {
            putValue(toKey(key), value)
        }
    }

    fun containsKey(key: String): Boolean {
        return preferences.contains(toKey(key))
    }

    fun removeValue(key: String) {
        preferences.edit {
            remove(toKey(key))
        }
    }

    @Throws(SecurePreferencesException::class)
    fun getString(key: String): String? {
        return if (preferences.contains(toKey(key))) {
            val securedEncodedValue = preferences.getString(toKey(key), "")
            try {
                decryptValue(securedEncodedValue)
            } catch (e: SecurePreferencesException) {
                removeValue(key)
                null
            }
        } else {
            null
        }
    }

    fun clear() {
        preferences.edit {
            clear()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        listeners[key]?.onSharedPreferenceChanged(sharedPreferences, decryptKey(key))
    }

    /**
     * Registers a callback to be invoked when a change happens to the specified
     * preference.
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
        listeners[toKey(key)] = listener
    }

    /**
     * Unregisters a previous callback.
     *
     * @param key PReference key, the callback of which that should be unregistered.
     * @see .registerOnSharedPreferenceChangeListener
     */
    fun unregisterOnSharedPreferenceChangeListener(key: String) {
        listeners.remove(toKey(key))
    }

    class SecurePreferencesException internal constructor(e: Throwable) : RuntimeException(e)

    companion object {
        private val TRANSFORMATION = "AES/CBC/PKCS5Padding"
        private val KEY_TRANSFORMATION = "AES/ECB/PKCS5Padding"
        private val SECRET_KEY_HASH_TRANSFORMATION = "SHA-256"
        private val CHARSET = "UTF-8"

        @Throws(SecurePreferencesException::class)
        private fun convert(cipher: Cipher, bs: ByteArray): ByteArray {
            try {
                return cipher.doFinal(bs)
            } catch (e: Exception) {
                throw SecurePreferencesException(e)
            }
        }
    }
}
