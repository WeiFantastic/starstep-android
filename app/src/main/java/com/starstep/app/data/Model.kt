package com.starstep.app.data

import kotlinx.serialization.Serializable

/**
 * 应用全量状态，整体序列化为 JSON 存入 DataStore。
 * 单机可用、离线优先：所有数据均在端侧，不依赖服务端。
 */
@Serializable
data class AppState(
    val actionPower: Int = 0,
    val routes: List<Route> = emptyList(),
    val stars: List<Star> = emptyList(),
    val energyRecords: List<EnergyRecord> = emptyList(),
    val settings: Settings = Settings(),
    val session: ExecutionSession? = null,
    val isSeeded: Boolean = false
)

/** 全局设置：三个可关闭的开关，尊重用户掌控感 */
@Serializable
data class Settings(
    val voiceGuide: Boolean = true,      // 语音引导
    val autoStart: Boolean = true,       // 到点自动开始第一步 / 自动推进
    val reduceMotion: Boolean = false,   // 减少动态效果
    val granularity: String = "coarse"   // coarse=粗一点(默认) / fine=细一点
)

/** 路线（SOP） */
@Serializable
data class Route(
    val id: String,
    val name: String,
    val steps: List<Step>,
    val colorKey: String = "sky",             // sky/cream/deep/mint/rose
    val reminderDays: List<Int> = emptyList(), // 1=周一 ... 7=周日
    val reminderTime: String = "",            // HH:mm
    val autoStart: Boolean = false,
    val estimatedMinutes: Int = 0,
    val totalCompletions: Int = 0,
    val streakDays: Int = 0,
    val lastDoneAt: Long = 0L,
    val recentDurations: List<Int> = emptyList()
)

/** 步骤 */
@Serializable
data class Step(
    val id: String,
    val title: String,
    val description: String = "",
    val options: List<String> = emptyList(),  // 可选方案，如 ["自己买","自己做"]
    val selectedOption: Int = 0,
    val durationSec: Int = 60                 // 本步计时（秒）
)

/** 星星：一次行动完成后折成，只增不减 */
@Serializable
data class Star(
    val id: String,
    val routeId: String,
    val routeName: String,
    val colorKey: String,
    val finishedAt: Long,
    val durationMinutes: Int,
    val note: String,          // 当时写给自己说的话
    val energyLabel: String,   // 当时状态档位
    val reply: String,         // 雪球回信（基于真实行为生成）
    val stepsTotal: Int
)

/** 精力记录：执行中 / 完成后 / 每日打卡三处汇聚到同一份周期数据 */
@Serializable
data class EnergyRecord(
    val id: String,
    val at: Long,
    val level: Int,        // 1=很低 2=一般 3=还不错 4=很好
    val source: String     // execute / complete / checkin
)

/** 执行会话：记录当前走到哪一步、本步计时起点，支持切后台继续 */
@Serializable
data class ExecutionSession(
    val routeId: String,
    val currentStep: Int,
    val stepStartedAt: Long,
    val startedAt: Long,
    val completedSteps: List<Int> = emptyList(),
    val active: Boolean = true
)

/** 星阶等级阈值：第4阶=30颗，第5阶「微光」=40颗，与原型对齐 */
object StarRanks {
    data class Rank(val level: Int, val name: String, val threshold: Int)

    val ranks = listOf(
        Rank(1, "起步", 0),
        Rank(2, "破土", 8),
        Rank(3, "抽芽", 18),
        Rank(4, "小小树", 30),
        Rank(5, "微光", 40),
        Rank(6, "星河", 54),
        Rank(7, "银河", 72),
        Rank(8, "璀璨", 96)
    )

    fun rankOf(starCount: Int): Rank {
        var current = ranks.first()
        for (r in ranks) if (starCount >= r.threshold) current = r
        return current
    }

    fun nextRank(starCount: Int): Rank? =
        ranks.firstOrNull { it.threshold > starCount }
}

/** 星星纸色卡映射：色系 -> 任务类型 */
object StarColors {
    const val SKY = "sky"       // 晴空蓝 = 晨间日常
    const val CREAM = "cream"   // 暖白 = 休息放松
    const val DEEP = "deep"     // 深海蓝 = 专注学习
    const val MINT = "mint"     // 薄荷绿 = 健身健康
    const val ROSE = "rose"     // 藕粉 = 社交情绪

    val labelMap = mapOf(
        SKY to "晨间日常",
        CREAM to "休息放松",
        DEEP to "专注学习",
        MINT to "健身健康",
        ROSE to "社交情绪"
    )

    val filterOptions = listOf("全部", SKY, CREAM, MINT, DEEP, ROSE)

    val filterLabel = mapOf(
        "全部" to "全部",
        SKY to "晴空蓝",
        CREAM to "暖白",
        MINT to "薄荷绿",
        DEEP to "深海蓝",
        ROSE to "藕粉"
    )
}

/** 四档情绪 */
object EnergyLevels {
    val options = listOf(
        Pair(1, "很低"), Pair(2, "一般"), Pair(3, "还不错"), Pair(4, "很好")
    )
    fun label(level: Int) = options.getOrNull(level - 1)?.second ?: "一般"
}
