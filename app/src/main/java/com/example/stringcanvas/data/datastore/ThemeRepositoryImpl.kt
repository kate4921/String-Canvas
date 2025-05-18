package com.example.stringcanvas.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.stringcanvas.domain.models.ThemeOption
import com.example.stringcanvas.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.map


class ThemeRepositoryImpl(
    private val store: DataStore<Preferences>
) : ThemeRepository {

    private val KEY = stringPreferencesKey("theme_option")

    override val themeFlow = store.data.map { prefs ->
        prefs[KEY]?.let(ThemeOption::valueOf) ?: ThemeOption.SYSTEM
    }

    override suspend fun setTheme(option: ThemeOption) {
        store.edit { it[KEY] = option.name }
    }
}

