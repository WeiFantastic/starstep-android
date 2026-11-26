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
import java.util.UUID

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
        val loaded = if (raw.isNullOrBlank()) {
            SeedData.build()
        } else {
            runCatching { json.decodeFromString<AppState>(raw) }
                .getOrElse { SeedData.build() }
        }
        // 关键修复：任何从磁盘读出的状态都先做一次迁移清洗。
        // 历史版本里路线/星星/步骤 ID 可能重复，LazyColumn 用它作 key 会抛
        // IllegalArgumentException: Key was already used，导致启动即崩溃。
        // 这里在数据源头保证所有 key 唯一，旧数据无需用户手动清缓存即可自愈。
        migrateLegacy(loaded)
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

    /**
     * 旧数据迁移：为任何重复/空白的 ID 重新分配 UUID，并修复指向失效路线的会话。
     * 纯函数，不写盘；读取时统一经过它，保证 UI 拿到的永远是可用状态。
     */
    private fun migrateLegacy(src: AppState): AppState {
        var changed = false

        // ---- 路线 ID 去重（保留首次出现，其余换新）----
        val seenRouteIds = HashSet<String>()
        val routes = src.routes.map { route ->
            val newId = if (route.id.isBlank() || !seenRouteIds.add(route.id)) {
                changed = true
                "r_${UUID.randomUUID()}"
            } else route.id

            // 步骤 ID 在同一条路线内也要唯一（跨路线的步骤 ID 相同不影响 key，
            // 但统一洗一遍可避免未来把步骤也用作 key 时踩坑）
            val seenStepIds = HashSet<String>()
            val steps = route.steps.map { step ->
                val newStepId = if (step.id.isBlank() || !seenStepIds.add(step.id)) {
                    changed = true
                    "s_${UUID.randomUUID()}"
                } else step.id
                if (newStepId != step.id) step.copy(id = newStepId) else step
            }
            if (newId != route.id || steps != route.steps) {
                route.copy(id = newId, steps = steps)
            } else route
        }

        // ---- 星星 ID 去重 ----
        val seenStarIds = HashSet<String>()
        val stars = src.stars.map { star ->
            val newId = if (star.id.isBlank() || !seenStarIds.add(star.id)) {
                changed = true
                "star_${UUID.randomUUID()}"
            } else star.id
            if (newId != star.id) star.copy(id = newId) else star
        }

        // ---- 精力记录 ID 去重 ----
        val seenEnergyIds = HashSet<String>()
        val energyRecords = src.energyRecords.map { record ->
            val newId = if (record.id.isBlank() || !seenEnergyIds.add(record.id)) {
                changed = true
                "e_${UUID.randomUUID()}"
            } else record.id
            if (newId != record.id) record.copy(id = newId) else record
        }

        // ---- 会话修复：指向已不存在路线的残留会话直接清掉 ----
        val validRouteIds = routes.mapTo(HashSet()) { it.id }
        val session = src.session?.takeIf { validRouteIds.contains(it.routeId) }
        if (session == null && src.session != null) changed = true

        return if (changed) {
            src.copy(
                routes = routes,
                stars = stars,
                energyRecords = energyRecords,
                session = session
            )
        } else src
    }
}
