package com.tecsup.visionastra.mobile.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

data class EncryptedTokenValue(
    val cipherText: String,
    val iv: String
)

@Singleton
class TokenCipher @Inject constructor() {
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    fun encrypt(value: String): EncryptedTokenValue {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

        return EncryptedTokenValue(
            cipherText = cipher.doFinal(value.toByteArray(Charsets.UTF_8)).toBase64(),
            iv = cipher.iv.toBase64()
        )
    }

    fun decrypt(value: EncryptedTokenValue): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.decode(value.iv, Base64.NO_WRAP))
        )

        return cipher.doFinal(Base64.decode(value.cipherText, Base64.NO_WRAP))
            .toString(Charsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) return existingKey.secretKey

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "visionastra_mobile_tokens"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
