package com.starstep.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.starstep.app.data.AppState
import com.starstep.app.data.EnergyLevels
import com.starstep.app.data.EnergyRecord
import com.starstep.app.data.ExecutionSession
import com.starstep.app.data.Route
import com.starstep.app.data.Settings
import com.starstep.app.data.Star
import com.starstep.app.data.StarStepRepository
import com.starstep.app.data.Step
import com.starstep.app.BuildConfig
import com.starstep.app.util.UpdateChecker
import com.starstep.app.util.UpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class StarStepViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = StarStepRepository(app.applicationContext)

    private val _state = MutableStateFlow<AppState?>(null)
    val state: StateFlow<AppState?> = _state.asStateFlow()

    // 一次性 UI 事件（Toast / 语音播报 / 导航），由界面 collect。
    // 带自增 id，保证连续两次同类事件也能各自被观察（不会因同值被 StateFlow 去重）。
    private val _event = MutableStateFlow<EventWrapper?>(null)
    val event: StateFlow<EventWrapper?> = _event.asStateFlow()
    private var eventId = 0L

    // 路线草案：首页输入 → 生成页之间的暂存（不持久化）
    private val _draft = MutableStateFlow<Route?>(null)
    val draft: StateFlow<Route?> = _draft.asStateFlow()

    // 新版本提示：启动自动检查 / 设置页手动检查后填充。null 表示无更新
    private val _update = MutableStateFlow<UpdateInfo?>(null)
    val update: StateFlow<UpdateInfo?> = _update.asStateFlow()

    init {
        viewModelScope.launch {
            repo.state.collect { _state.value = it }
        }
        // 启动静默检查一次（失败自动忽略，不打扰用户）
        checkUpdate()
    }

    /** 检查新版本。静默容错：失败不提示、不报错 */
    fun checkUpdate() {
        viewModelScope.launch {
            val info = UpdateChecker.check(BuildConfig.VERSION_CODE)
            if (info != null) _update.value = info
        }
    }

    /** 关闭更新弹窗（非强制时可稍后再说） */
    fun dismissUpdate() { _update.value = null }

    private fun update(transform: (AppState) -> AppState) {
        val current = _state.value ?: return
        val next = transform(current)
        _state.value = next
        viewModelScope.launch { repo.save(next) }
    }

    fun emit(event: UiEvent) {
        _event.value = EventWrapper(++eventId, event)
    }

    fun consumeEvent() { _event.value = null }

    // ---------- 意图与路线 ----------

    /** 根据「现状→目标」和粒度生成路线草案 */
    fun generateRoute(current: String, goal: String): Route {
        val s = _state.value?.settings ?: Settings()
        val coarse = s.granularity == "coarse"
        val target = goal.ifBlank { "离开${current.ifBlank { "现在" }}" }
        val steps = if (coarse) {
            listOf(
                Step(uid(), "先离开${current.ifBlank { "原地" }}", "只做这一步，别想后面", durationSec = 60),
                Step(uid(), "做最容易开始的那一小步", "别追求完整，先写、先读、先做第一题", durationSec = 300),
                Step(uid(), "再往前推一点点", "做到这里就已经很好了", durationSec = 300)
            )
        } else {
            listOf(
                Step(uid(), "先坐起来", "", durationSec = 30),
                Step(uid(), "把要用的东西放到手边", "", durationSec = 60),
                Step(uid(), "离开${current.ifBlank { "原地" }}", "", durationSec = 60),
                Step(uid(), "做最容易开始的那一小步", "先写、先读、先做第一题", durationSec = 180),
                Step(uid(), "继续往下做一题", "", durationSec = 300),
                Step(uid(), "检查一遍", "", durationSec = 180),
                Step(uid(), "收尾", "做到这里就够了", durationSec = 120)
            )
        }
        return Route(
            id = uid(),
            name = deriveName(target),
            steps = steps,
            colorKey = com.starstep.app.data.StarColors.DEEP,
            estimatedMinutes = steps.sumOf { it.durationSec } / 60 + 1
        )
    }

    /** 生成并暂存路线草案（首页 → 生成页） */
    fun prepareDraft(current: String, goal: String): Route {
        val route = generateRoute(current, goal)
        _draft.value = route
        return route
    }

    fun getDraft(): Route? = _draft.value
    fun clearDraft() { _draft.value = null }

    private fun deriveName(goal: String): String {
        val g = goal.trim()
        return when {
            g.length <= 12 -> g
            g.length <= 22 -> g
            else -> g.take(20) + "…"
        }
    }

    fun saveRoute(route: Route) {
        update { st -> st.copy(routes = listOf(route) + st.routes) }
    }

    fun updateRoute(route: Route) {
        update { st -> st.copy(routes = st.routes.map { if (it.id == route.id) route else it }) }
    }

    fun deleteRoute(routeId: String) {
        update { st -> st.copy(routes = st.routes.filterNot { it.id == routeId }) }
    }

    // ---------- 执行会话 ----------

    fun startRoute(routeId: String) {
        val route = _state.value?.routes?.firstOrNull { it.id == routeId } ?: return
        val now = System.currentTimeMillis()
        val session = ExecutionSession(
            routeId = routeId,
            currentStep = 0,
            stepStartedAt = now,
            startedAt = now
        )
        update { st -> st.copy(session = session) }
        speak(route.steps.firstOrNull()?.title.orEmpty())
    }

    fun abandonSession() {
        update { st -> st.copy(session = null) }
    }

    /** 完成当前步：行动力+2，进入下一步；若为最后一步则触发折星 */
    fun completeStep(): StepResult {
        val st = _state.value ?: return StepResult.Error
        val session = st.session ?: return StepResult.Error
        val route = st.routes.firstOrNull { it.id == session.routeId } ?: return StepResult.Error
        val completed = session.completedSteps + session.currentStep
        val next = session.currentStep + 1
        val now = System.currentTimeMillis()

        return if (next >= route.steps.size) {
            // 全部完成 —— 更新路线统计，会话进入「待折星」由界面处理
            update { s ->
                s.copy(
                    actionPower = s.actionPower + 2,
                    routes = s.routes.map {
                        if (it.id == route.id) it.copy(
                            totalCompletions = it.totalCompletions + 1,
                            streakDays = it.streakDays + 1,
                            lastDoneAt = now,
                            recentDurations = (listOf(((now - session.startedAt) / 60000).toInt().coerceAtLeast(1)) + it.recentDurations).take(5)
                        ) else it
                    },
                    session = session.copy(
                        currentStep = next,
                        completedSteps = completed,
                        stepStartedAt = now,
                        active = false
                    )
                )
            }
            StepResult.RouteFinished(route)
        } else {
            update { s ->
                s.copy(
                    actionPower = s.actionPower + 2,
                    session = session.copy(
                        currentStep = next,
                        completedSteps = completed,
                        stepStartedAt = now
                    )
                )
            }
            speak(route.steps[next].title)
            StepResult.NextStep(route.steps[next])
        }
    }

    /** 遇到困难：跳过本步，不给惩罚，只降低步长建议 */
    fun skipStep(): StepResult {
        val st = _state.value ?: return StepResult.Error
        val session = st.session ?: return StepResult.Error
        val route = st.routes.firstOrNull { it.id == session.routeId } ?: return StepResult.Error
        val next = session.currentStep + 1
        emit(UiEvent.Toast("没关系，跳过也算数"))
        return if (next >= route.steps.size) {
            StepResult.RouteFinished(route)
        } else {
            update { s ->
                s.copy(
                    session = session.copy(
                        currentStep = next,
                        completedSteps = session.completedSteps + session.currentStep,
                        stepStartedAt = System.currentTimeMillis()
                    )
                )
            }
            StepResult.NextStep(route.steps[next])
        }
    }

    /** 现场调整：保存步骤列表改动，已完成步保留 */
    fun saveAdjustedSteps(routeId: String, steps: List<Step>) {
        update { st ->
            st.copy(routes = st.routes.map { if (it.id == routeId) it.copy(steps = steps) else it })
        }
        emit(UiEvent.Toast("已保存调整"))
    }

    // ---------- 折星 ----------

    /** 把完成的路线折成一颗星，写入星星墙；回信基于真实行为数据 */
    fun foldStar(note: String, energyLevel: Int): Star? {
        val st = _state.value ?: return null
        val session = st.session ?: return null
        val route = st.routes.firstOrNull { it.id == session.routeId } ?: return null
        val duration = ((System.currentTimeMillis() - session.startedAt) / 60000).toInt().coerceAtLeast(1)
        val energyLabel = EnergyLevels.label(energyLevel)
        val star = Star(
            id = uid(),
            routeId = route.id,
            routeName = route.name,
            colorKey = route.colorKey,
            finishedAt = System.currentTimeMillis(),
            durationMinutes = duration,
            note = note,
            energyLabel = energyLabel,
            reply = buildReply(route, duration, energyLevel),
            stepsTotal = route.steps.size
        )
        val energyRecord = EnergyRecord(
            id = uid(), at = System.currentTimeMillis(), level = energyLevel, source = "complete"
        )
        update { s ->
            s.copy(
                stars = listOf(star) + s.stars,
                energyRecords = listOf(energyRecord) + s.energyRecords,
                session = null
            )
        }
        return star
    }

    /** 雪球回信：基于用时、频次、当时状态，不做空洞鼓励 */
    private fun buildReply(route: Route, duration: Int, energyLevel: Int): String {
        return when {
            energyLevel <= 2 && duration <= route.estimatedMinutes -> "状态不高还是走完了，这颗星最亮 ✦"
            duration <= route.estimatedMinutes -> "你看，起点没那么难吧 ✦"
            route.totalCompletions >= 5 -> "这条路你走过 ${route.totalCompletions} 次了，越来越顺了"
            else -> "每一步都算数，我看见了 ✦"
        }
    }

    /** 拆星撤销：回到待办，行动力和统计回退 */
    fun undoFold(star: Star) {
        update { st ->
            st.copy(
                stars = st.stars.filterNot { it.id == star.id },
                actionPower = (st.actionPower - star.stepsTotal * 2).coerceAtLeast(0),
                routes = st.routes.map {
                    if (it.id == star.routeId) it.copy(
                        totalCompletions = (it.totalCompletions - 1).coerceAtLeast(0)
                    ) else it
                }
            )
        }
        emit(UiEvent.Toast("已拆回，随时可以再来"))
    }

    // ---------- 精力 ----------

    fun checkIn(level: Int) {
        val record = EnergyRecord(uid(), System.currentTimeMillis(), level, "checkin")
        update { st -> st.copy(energyRecords = listOf(record) + st.energyRecords) }
        emit(UiEvent.Toast("已记入精力周期"))
    }

    fun recordEnergyDuringExecute(level: Int) {
        val record = EnergyRecord(uid(), System.currentTimeMillis(), level, "execute")
        update { st -> st.copy(energyRecords = listOf(record) + st.energyRecords) }
    }

    /** 精力洞察：按小时段聚合，找出高频高能时段；样本不足时返回 null */
    fun energyInsight(): EnergyInsight? {
        val records = _state.value?.energyRecords ?: return null
        if (records.size < 8) return null
        val buckets = records.groupBy { (it.at / 3_600_000L) % 24 }
            .mapValues { (_, list) -> list.map { it.level }.average() }
        val bestHour = (buckets.maxByOrNull { it.value }?.key ?: return null).toInt()
        val avg = buckets[bestHour.toLong()] ?: return null
        val start = bestHour
        val end = (bestHour + 5) % 24
        return EnergyInsight(start, end, avg)
    }

    // ---------- 设置 ----------

    fun setGranularity(value: String) {
        update { st -> st.copy(settings = st.settings.copy(granularity = value)) }
    }

    fun toggleVoice() {
        update { st -> st.copy(settings = st.settings.copy(voiceGuide = !st.settings.voiceGuide)) }
    }

    fun toggleAutoStart() {
        update { st -> st.copy(settings = st.settings.copy(autoStart = !st.settings.autoStart)) }
    }

    fun toggleReduceMotion() {
        update { st -> st.copy(settings = st.settings.copy(reduceMotion = !st.settings.reduceMotion)) }
    }

    fun resetAll() {
        viewModelScope.launch { repo.resetToSeed() }
    }

    // ---------- 语音 ----------

    fun speak(text: String) {
        if (_state.value?.settings?.voiceGuide != true || text.isBlank()) return
        emit(UiEvent.Speak(text))
    }

    /** 打开外部链接（下载页 / QQ 反馈） */
    fun openUrl(url: String) { emit(UiEvent.OpenUrl(url)) }

    private fun uid(): String = UUID.randomUUID().toString()
}

// ---------- 结果 / 事件类型 ----------

sealed interface StepResult {
    data class NextStep(val step: Step) : StepResult
    data class RouteFinished(val route: Route) : StepResult
    data object Error : StepResult
}

sealed interface UiEvent {
    data class Toast(val text: String) : UiEvent
    data class Speak(val text: String) : UiEvent
    data class OpenUrl(val url: String) : UiEvent  // 打开外部链接（下载页 / QQ 反馈）
}

/** 一次性事件包装：id 保证每次 emit 都是新值，避免 StateFlow 同值去重导致事件丢失 */
data class EventWrapper(val id: Long, val event: UiEvent)

data class EnergyInsight(val startHour: Int, val endHour: Int, val avgLevel: Double)
