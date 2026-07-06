package com.tecsup.visionastra.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tecsup.visionastra.mobile.core.security.EncryptedTokenValue
import com.tecsup.visionastra.mobile.core.security.TokenCipher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.tokenDataStore by preferencesDataStore(name = "secure_token_preferences")

@Singleton
class TokenStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val tokenCipher: TokenCipher
) {
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        val encryptedAccessToken = tokenCipher.encrypt(accessToken)
        val encryptedRefreshToken = tokenCipher.encrypt(refreshToken)

        context.tokenDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = encryptedAccessToken.cipherText
            preferences[ACCESS_TOKEN_IV] = encryptedAccessToken.iv
            preferences[REFRESH_TOKEN] = encryptedRefreshToken.cipherText
            preferences[REFRESH_TOKEN_IV] = encryptedRefreshToken.iv
        }
    }

    suspend fun getAccessToken(): String? {
        val preferences = context.tokenDataStore.data.first()
        val cipherText = preferences[ACCESS_TOKEN] ?: return null
        val iv = preferences[ACCESS_TOKEN_IV] ?: return null
        return runCatching {
            tokenCipher.decrypt(EncryptedTokenValue(cipherText, iv))
        }.getOrNull()
    }

    suspend fun getRefreshToken(): String? {
        val preferences = context.tokenDataStore.data.first()
        val cipherText = preferences[REFRESH_TOKEN] ?: return null
        val iv = preferences[REFRESH_TOKEN_IV] ?: return null
        return runCatching {
            tokenCipher.decrypt(EncryptedTokenValue(cipherText, iv))
        }.getOrNull()
    }

    suspend fun hasTokens(): Boolean {
        val preferences = context.tokenDataStore.data.first()
        return preferences[ACCESS_TOKEN] != null &&
            preferences[ACCESS_TOKEN_IV] != null &&
            preferences[REFRESH_TOKEN] != null &&
            preferences[REFRESH_TOKEN_IV] != null
    }

    suspend fun clearTokens() {
        context.tokenDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(ACCESS_TOKEN_IV)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(REFRESH_TOKEN_IV)
        }
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token_cipher_text")
        val ACCESS_TOKEN_IV = stringPreferencesKey("access_token_iv")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token_cipher_text")
        val REFRESH_TOKEN_IV = stringPreferencesKey("refresh_token_iv")
    }
}
