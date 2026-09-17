package com.example.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.localization.AppLanguage
import com.example.model.UserRole
import com.example.model.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "educore_prefs")

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

class AppPreferences(private val context: Context, private val scope: CoroutineScope) {

    private val _language = MutableStateFlow(AppLanguage.ARABIC)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            val prefs = context.dataStore.data.first()
            val langCode = prefs[KEY_LANGUAGE] ?: AppLanguage.ARABIC.code
            _language.value = if (langCode == "en") AppLanguage.ENGLISH else AppLanguage.ARABIC

            val themeName = prefs[KEY_THEME] ?: ThemeMode.LIGHT.name
            _themeMode.value = try {
                ThemeMode.valueOf(themeName)
            } catch (e: Exception) {
                ThemeMode.LIGHT
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        scope.launch(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                prefs[KEY_LANGUAGE] = lang.code
            }
        }
    }

    fun toggleLanguage() {
        val newLang = if (_language.value == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
        setLanguage(newLang)
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        scope.launch(Dispatchers.IO) {
            context.dataStore.edit { prefs ->
                prefs[KEY_THEME] = mode.name
            }
        }
    }

    fun toggleTheme() {
        val newTheme = if (_themeMode.value == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(newTheme)
    }

    fun setUserSession(session: UserSession?) {
        _currentSession.value = session
    }

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_THEME = stringPreferencesKey("app_theme_mode")
    }
}
