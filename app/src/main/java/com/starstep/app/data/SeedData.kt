package com.starstep.app.data

import kotlin.random.Random

/**
 * 首次启动的种子数据，与交互原型对齐：8 条路线、34 颗星、近 7 天精力记录。
 * 让新用户打开即看到「走过的路」，而不是空壳。所有数值本地生成，无网络依赖。
 */
object SeedData {

    fun build(): AppState {
        val now = System.currentTimeMillis()
        val routes = buildRoutes()
        val stars = buildStars(routes, now)
        val energy = buildEnergy(now)
        return AppState(
            actionPower = 34 * 8 * 2, // 34颗星×约8步×每步+2，象征性累计
            routes = routes,
            stars = stars,
            energyRecords = energy,
            settings = Settings(),
            isSeeded = true
        )
    }

    private fun buildRoutes(): List<Route> = listOf(
        Route(
            id = "r_morning", name = "晨间启动 SOP", colorKey = StarColors.SKY,
            steps = listOf(
                Step("s1", "先坐起来", "别想今天，先把身体支起来", durationSec = 30),
                Step("s2", "喝一口水", "床头备好的那杯", durationSec = 30),
                Step("s3", "拉开窗帘", "让光进来", durationSec = 45),
                Step("s4", "换好衣服", "", durationSec = 180),
                Step("s5", "做最容易开始的那一小步", "别追求完整，先写、先读、先做第一题", durationSec = 300)
            ),
            reminderDays = listOf(1, 2, 3, 4, 5), reminderTime = "07:30", autoStart = true,
            estimatedMinutes = 44, totalCompletions = 12, streakDays = 6, lastDoneAt = todayAt(7, 46),
            recentDurations = listOf(47, 45, 44, 46, 43)
        ),
        Route(
            id = "r_sleep", name = "睡前放松", colorKey = StarColors.CREAM,
            steps = listOf(
                Step("s1", "放下手机", "放到够不着的地方", durationSec = 60),
                Step("s2", "调暗灯光", "", durationSec = 30),
                Step("s3", " stretch 一下", "伸个懒腰就好", durationSec = 120),
                Step("s4", "想一件今天还不错的事", "", durationSec = 90)
            ),
            reminderDays = listOf(1, 2, 3, 4, 5, 6, 7), reminderTime = "22:30",
            estimatedMinutes = 12, totalCompletions = 9, streakDays = 3, lastDoneAt = yesterdayAt(22, 30),
            recentDurations = listOf(13, 12, 11)
        ),
        Route(
            id = "r_report", name = "写报告 · 从刷手机开始", colorKey = StarColors.DEEP,
            steps = listOf(
                Step("s1", "离开手机", "先把它翻面放好", durationSec = 60),
                Step("s2", "打开文档", "只打开，不要求写", durationSec = 60),
                Step("s3", "写下第一行的标题", "", durationSec = 180),
                Step("s4", "先把第一题写完", "慢慢来", durationSec = 600)
            ),
            estimatedMinutes = 25, totalCompletions = 3, streakDays = 0, lastDoneAt = daysAgoAt(3, 15, 20),
            recentDurations = listOf(28, 25)
        ),
        Route(
            id = "r_out", name = "出门准备", colorKey = StarColors.MINT,
            steps = listOf(
                Step("s1", "拿起钥匙", "", durationSec = 30),
                Step("s2", "穿鞋", "", durationSec = 120),
                Step("s3", "检查随身物品", "", durationSec = 60)
            ),
            estimatedMinutes = 15, totalCompletions = 8, streakDays = 2, lastDoneAt = yesterdayAt(8, 20),
            recentDurations = listOf(16, 15, 14)
        ),
        Route(
            id = "r_focus", name = "专注学习 25 分钟", colorKey = StarColors.DEEP,
            steps = listOf(
                Step("s1", "计时器按下开始", "", durationSec = 30),
                Step("s2", "只看第一页", "", durationSec = 600),
                Step("s3", "写下三个关键词", "", durationSec = 300),
                Step("s4", "停下来喝口水", "", durationSec = 120)
            ),
            estimatedMinutes = 25, totalCompletions = 6, streakDays = 1, lastDoneAt = daysAgoAt(2, 14, 10),
            recentDurations = listOf(26, 25)
        ),
        Route(
            id = "r_workout", name = "健身 15 分钟", colorKey = StarColors.MINT,
            steps = listOf(
                Step("s1", "换上运动服", "", durationSec = 180),
                Step("s2", "热身拉伸", "", durationSec = 180),
                Step("s3", "完成一组动作", "", durationSec = 600),
                Step("s4", "放松", "", durationSec = 120)
            ),
            estimatedMinutes = 15, totalCompletions = 5, streakDays = 0, lastDoneAt = daysAgoAt(4, 18, 30),
            recentDurations = listOf(17, 15)
        ),
        Route(
            id = "r_social", name = "回一条消息", colorKey = StarColors.ROSE,
            steps = listOf(
                Step("s1", "点开对话框", "", durationSec = 30),
                Step("s2", "先打一句「在」", "", durationSec = 60),
                Step("s3", "把想说的发出去", "", durationSec = 120)
            ),
            estimatedMinutes = 6, totalCompletions = 4, streakDays = 0, lastDoneAt = daysAgoAt(5, 20, 5),
            recentDurations = listOf(7, 6)
        ),
        Route(
            id = "r_tidy", name = "收拾桌面一角", colorKey = StarColors.CREAM,
            steps = listOf(
                Step("s1", "拿起手边的垃圾", "", durationSec = 60),
                Step("s2", "把杯子归位", "", durationSec = 60),
                Step("s3", "摆好一样东西", "", durationSec = 120)
            ),
            estimatedMinutes = 8, totalCompletions = 7, streakDays = 0, lastDoneAt = daysAgoAt(1, 21, 15),
            recentDurations = listOf(9, 8)
        )
    )

    private fun buildStars(routes: List<Route>, now: Long): List<Star> {
        val notes = listOf(
            "慢慢来，先把第一题写完就好", "今天真的动起来了", "起点没那么难",
            "允许自己只做一点点", "做完了，虽然很慢", "先做最容易的那一步",
            "状态一般，但真的开始了", "又走完一条，攒着"
        )
        val replies = listOf(
            "你看，起点没那么难吧 ✦", "每一步都算数，我在看着呢", "你比昨天多走了一步",
            "慢一点也没关系，方向是对的", "这一颗，是你自己挣来的"
        )
        val colors = listOf(
            StarColors.SKY, StarColors.CREAM, StarColors.MINT, StarColors.DEEP, StarColors.ROSE
        )
        val levels = listOf(1, 2, 3, 4)
        val random = Random(20261126)
        return (0 until 34).map { i ->
            val route = routes[i % routes.size]
            val finished = now - (i.toLong() * 6 * 60 * 60 * 1000L) - random.nextInt(0, 40) * 60_000L
            Star(
                id = "star_$i",
                routeId = route.id,
                routeName = route.name,
                colorKey = colors[i % colors.size],
                finishedAt = finished,
                durationMinutes = route.estimatedMinutes + random.nextInt(-3, 5),
                note = notes[i % notes.size],
                energyLabel = EnergyLevels.label(levels[i % levels.size]),
                reply = replies[i % replies.size],
                stepsTotal = route.steps.size
            )
        }.sortedByDescending { it.finishedAt }
    }

    /** 近 7 天精力记录：每天有真实波动、午后偏高，呼应洞察卡与曲线柱高 */
    private fun buildEnergy(now: Long): List<EnergyRecord> {
        val list = mutableListOf<EnergyRecord>()
        // 每天的基础精力不同，让 7 根柱子有高低（修复 BUG-12：原来每天平均都是 3.0 → 等高）
        val dayBase = listOf(2, 4, 3, 4, 3, 2, 3) // 对应 6 天前 → 今天
        for (d in 6 downTo 0) {
            for (slot in 0..2) {
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = now
                cal.add(java.util.Calendar.DAY_OF_YEAR, -d)
                val hours = listOf(9, 15, 20)
                val h = hours[slot]
                cal.set(java.util.Calendar.HOUR_OF_DAY, h)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                // 午后 +1（封顶 4），早晚按当天基础值轻微波动
                val base = dayBase[6 - d]
                val level = when (h) {
                    15 -> minOf(4, base + 1)
                    9 -> maxOf(1, base - 1)
                    else -> base
                }
                list.add(
                    EnergyRecord(
                        id = "e_${d}_$h",
                        at = cal.timeInMillis,
                        level = level,
                        source = "checkin"
                    )
                )
            }
        }
        return list
    }

    private fun todayAt(h: Int, m: Int): Long = dayAt(0, h, m)
    private fun yesterdayAt(h: Int, m: Int): Long = dayAt(1, h, m)
    private fun daysAgoAt(d: Int, h: Int, m: Int): Long = dayAt(d, h, m)

    private fun dayAt(daysAgo: Int, h: Int, m: Int): Long {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
        cal.set(java.util.Calendar.HOUR_OF_DAY, h)
        cal.set(java.util.Calendar.MINUTE, m)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
