package com.tecsup.visionastra.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tecsup.visionastra.mobile.core.session.AuthUser
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val userDarkModeEnabled: Flow<Boolean?> =
        context.appPreferencesDataStore.data.map { preferences ->
            preferences[USER_DARK_MODE_ENABLED]
        }

    suspend fun setUserDarkModeEnabled(enabled: Boolean) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[USER_DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun clearUserDarkModePreference() {
        context.appPreferencesDataStore.edit { preferences ->
            preferences.remove(USER_DARK_MODE_ENABLED)
        }
    }

    suspend fun saveAuthenticatedUser(user: AuthUser) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[USER_ID] = user.idUsuario
            preferences[USER_NAMES] = user.nombres
            preferences[USER_LAST_NAMES] = user.apellidos.orEmpty()
            preferences[USER_EMAIL] = user.email
            preferences[USER_ROLE] = user.rol
            preferences[USER_STATUS] = user.estado
        }
    }

    suspend fun getAuthenticatedUser(): AuthUser? {
        val preferences = context.appPreferencesDataStore.data.first()
        return AuthUser(
            idUsuario = preferences[USER_ID] ?: return null,
            nombres = preferences[USER_NAMES] ?: return null,
            apellidos = preferences[USER_LAST_NAMES].orEmpty().ifBlank { null },
            email = preferences[USER_EMAIL] ?: return null,
            rol = preferences[USER_ROLE] ?: return null,
            estado = preferences[USER_STATUS] ?: return null
        )
    }

    suspend fun clearAuthenticatedUser() {
        context.appPreferencesDataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(USER_NAMES)
            preferences.remove(USER_LAST_NAMES)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_ROLE)
            preferences.remove(USER_STATUS)
        }
    }

    private companion object {
        val USER_DARK_MODE_ENABLED = booleanPreferencesKey("user_dark_mode_enabled")
        val USER_ID = longPreferencesKey("auth_user_id")
        val USER_NAMES = stringPreferencesKey("auth_user_names")
        val USER_LAST_NAMES = stringPreferencesKey("auth_user_last_names")
        val USER_EMAIL = stringPreferencesKey("auth_user_email")
        val USER_ROLE = stringPreferencesKey("auth_user_role")
        val USER_STATUS = stringPreferencesKey("auth_user_status")
    }
}
