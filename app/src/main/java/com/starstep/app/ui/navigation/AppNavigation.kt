package com.starstep.app.ui.navigation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.starstep.app.ui.components.BottomTabBar
import com.starstep.app.ui.components.ToastHost
import com.starstep.app.ui.theme.Deep
import com.starstep.app.ui.theme.Ink
import com.starstep.app.ui.screens.*
import com.starstep.app.util.TtsHelper
import com.starstep.app.viewmodel.StarStepViewModel
import com.starstep.app.viewmodel.UiEvent
import androidx.compose.ui.platform.LocalContext

/** 导航路由常量 */
object Routes {
    const val HOME = "home"
    const val STAR_WALL = "starwall"
    const val ROUTES = "routes"
    const val ENERGY = "energy"
    const val GENERATE = "generate"
    const val ROUTE_DETAIL = "route_detail"
    const val EXECUTE = "execute"
    const val FOLD = "fold"
    const val RECEIPT = "receipt"
    const val ADJUST = "adjust"
    const val SETTINGS = "settings"

    fun withArg(route: String, arg: String) = "$route/$arg"
    fun argRoute(route: String) = "$route/{arg}"
}

@Composable
fun AppNavigation(vm: StarStepViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by vm.state.collectAsState()
    val event by vm.event.collectAsState()
    val update by vm.update.collectAsState()
    val context = LocalContext.current
    val tts = remember { TtsHelper(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    // 打开外部链接（下载页 / QQ 反馈），无对应 App 时给出提示
    fun openExternal(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (t: Throwable) {
            Toast.makeText(context, "没有可打开该链接的应用", Toast.LENGTH_SHORT).show()
        }
    }

    // 处理一次性事件：Toast + 语音 + 外部链接
    LaunchedEffect(event) {
        when (val e = event?.event) {
            is UiEvent.Toast -> snackbarHostState.showSnackbar(e.text)
            is UiEvent.Speak -> tts.speak(e.text)
            is UiEvent.OpenUrl -> openExternal(e.url)
            null -> Unit
        }
        if (event != null) vm.consumeEvent()
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Routes.HOME
    val showTab = currentRoute in listOf(Routes.HOME, Routes.STAR_WALL, Routes.ROUTES, Routes.ENERGY)

    if (state == null) {
        Box(Modifier.fillMaxSize()) // 首次加载占位
        return
    }
    val appState = state!!

    // 发现新版本时的提示弹窗（静默检查结果，无更新时不显示）
    if (update != null) {
        val info = update!!
        AlertDialog(
            onDismissRequest = { if (!info.forceUpdate) vm.dismissUpdate() },
            title = { androidx.compose.material3.Text("发现新版本 v${info.versionName}") },
            text = {
                androidx.compose.material3.Text(
                    if (info.changelog.isNotBlank()) info.changelog else "有一些新改进，去下载页看看？"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val url = info.downloadPage.ifBlank { info.downloadUrl }
                    vm.dismissUpdate()
                    if (url.isNotBlank()) openExternal(url)
                }) { androidx.compose.material3.Text("去更新") }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissUpdate() }) { androidx.compose.material3.Text("稍后") }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Deep,
                    contentColor = Ink
                )
            }
        },
        bottomBar = {
            if (showTab) {
                BottomTabBar(
                    current = when (currentRoute) {
                        Routes.STAR_WALL -> "starwall"
                        Routes.ROUTES -> "routes"
                        Routes.ENERGY -> "energy"
                        else -> "home"
                    },
                    onSelect = { key ->
                        val dest = when (key) {
                            "starwall" -> Routes.STAR_WALL
                            "routes" -> Routes.ROUTES
                            "energy" -> Routes.ENERGY
                            else -> Routes.HOME
                        }
                        navController.navigate(dest) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = Routes.HOME) {
                composable(Routes.HOME) {
                    HomeScreen(
                        state = appState,
                        vm = vm,
                        onGenerate = { navController.navigate(Routes.GENERATE) },
                        onOpenRoute = { id -> navController.navigate(Routes.withArg(Routes.ROUTE_DETAIL, id)) },
                        onExecute = { id -> navController.navigate(Routes.withArg(Routes.EXECUTE, id)) },
                        onSettings = { navController.navigate(Routes.SETTINGS) }
                    )
                }
                composable(Routes.GENERATE) {
                    GenerateScreen(
                        vm = vm,
                        onStartExecute = { id -> navController.navigate(Routes.withArg(Routes.EXECUTE, id)) },
                        onAdjust = { id -> navController.navigate(Routes.withArg(Routes.ADJUST, id)) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.ROUTES) {
                    RoutesScreen(
                        state = appState,
                        onOpenRoute = { id -> navController.navigate(Routes.withArg(Routes.ROUTE_DETAIL, id)) },
                        onExecute = { id -> navController.navigate(Routes.withArg(Routes.EXECUTE, id)) },
                        onNew = { navController.navigate(Routes.HOME) }
                    )
                }
                composable(Routes.argRoute(Routes.ROUTE_DETAIL)) { entry ->
                    val id = entry.arguments?.getString("arg").orEmpty()
                    RouteDetailScreen(
                        routeId = id,
                        state = appState,
                        vm = vm,
                        onExecute = { navController.navigate(Routes.withArg(Routes.EXECUTE, id)) },
                        onAdjust = { navController.navigate(Routes.withArg(Routes.ADJUST, id)) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.argRoute(Routes.EXECUTE)) { entry ->
                    val id = entry.arguments?.getString("arg").orEmpty()
                    ExecuteScreen(
                        routeId = id,
                        state = appState,
                        vm = vm,
                        // 完成最后一步进入折星页时移除执行页：
                        // 避免完成后按返回又退回执行页、被 LaunchedEffect 重新开一局，
                        // 也避免 FOLD/EXECUTE 在栈里交替残留
                        onFold = {
                            navController.navigate(Routes.FOLD) {
                                popUpTo(Routes.withArg(Routes.EXECUTE, id)) { inclusive = true }
                            }
                        },
                        onAdjust = { navController.navigate(Routes.withArg(Routes.ADJUST, id)) },
                        // 中途退出：若处于「待折星」完成态，顺手清掉会话，防止下次启动残留
                        onBack = {
                            if (appState.session?.active == false) vm.abandonSession()
                            navController.popBackStack()
                        }
                    )
                }
                composable(Routes.FOLD) {
                    FoldScreen(
                        state = appState,
                        vm = vm,
                        onStarWall = {
                            // 兜底：收进星阶时若会话仍在（例如动画被跳过），确保清掉
                            if (appState.session != null) vm.abandonSession()
                            navController.navigate(Routes.STAR_WALL) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                            }
                        },
                        // 写留言：折星已在页面内完成，同时把折星页移出栈，
                        // 这样从回执页返回不会再退到「0/0 空完成页」
                        onWriteNote = { starId ->
                            navController.navigate(Routes.withArg(Routes.RECEIPT, starId)) {
                                popUpTo(Routes.FOLD) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.STAR_WALL) {
                    StarWallScreen(
                        state = appState,
                        vm = vm,
                        onOpenStar = { id -> navController.navigate(Routes.withArg(Routes.RECEIPT, id)) }
                    )
                }
                composable(Routes.argRoute(Routes.RECEIPT)) { entry ->
                    val id = entry.arguments?.getString("arg").orEmpty()
                    ReceiptScreen(
                        starId = id,
                        state = appState,
                        vm = vm,
                        onAgain = { routeId -> navController.navigate(Routes.withArg(Routes.EXECUTE, routeId)) },
                        // 回执页返回统一回星阶墙，避免退回空的 FOLD 完成页；
                        // 从星阶墙点开星星进来时，popBackStack 也能正确回到星阶墙
                        onBack = {
                            val prev = navController.previousBackStackEntry?.destination?.route
                            if (prev == Routes.FOLD || prev == Routes.argRoute(Routes.EXECUTE)) {
                                navController.navigate(Routes.STAR_WALL) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.popBackStack()
                            }
                        }
                    )
                }
                composable(Routes.argRoute(Routes.ADJUST)) { entry ->
                    val id = entry.arguments?.getString("arg").orEmpty()
                    AdjustScreen(
                        routeId = id,
                        state = appState,
                        vm = vm,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.ENERGY) {
                    EnergyScreen(state = appState, vm = vm)
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(state = appState, vm = vm, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
