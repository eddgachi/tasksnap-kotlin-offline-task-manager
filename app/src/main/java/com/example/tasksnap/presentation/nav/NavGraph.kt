package com.example.tasksnap.presentation.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tasksnap.presentation.ui.screen.TaskDetailScreen
import com.example.tasksnap.presentation.ui.screen.TaskListScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "task_list",
    ) {
        composable("task_list") {
            TaskListScreen(
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate("task_detail/$taskId")
                },
                onNavigateToCreateTask = {
                    navController.navigate("task_detail/new")
                },
            )
        }

        composable(
            route = "task_detail/{taskId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: "new"
            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
