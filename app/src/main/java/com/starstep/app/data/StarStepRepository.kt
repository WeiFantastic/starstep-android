package com.starstep.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "starstep_state")

/** 全量状态的本地仓库：DataStore + JSON，离线优先 */
class StarStepRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }
    private val stateKey = stringPreferencesKey("app_state_v1")

    val state: Flow<AppState> = context.dataStore.data.map { prefs ->
        val raw = prefs[stateKey]
        if (raw.isNullOrBlank()) {
            SeedData.build()
        } else {
            runCatching { json.decodeFromString<AppState>(raw) }
                .getOrElse { SeedData.build() }
        }
    }

    suspend fun save(state: AppState) {
        context.dataStore.edit { prefs ->
            prefs[stateKey] = json.encodeToString(state)
        }
    }

    suspend fun resetToSeed() {
        context.dataStore.edit { prefs ->
            prefs[stateKey] = json.encodeToString(SeedData.build())
        }
    }
}
