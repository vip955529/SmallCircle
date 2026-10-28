package com.techvipin130524.smallcircle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.techvipin130524.smallcircle.data.InMemoryGroupRepository
import com.techvipin130524.smallcircle.geofence.GeofenceManager
import com.techvipin130524.smallcircle.ui.GroupViewModel
import com.techvipin130524.smallcircle.ui.GroupViewModelFactory
import com.techvipin130524.smallcircle.ui.dashboard.GroupDashboardScreen
import com.techvipin130524.smallcircle.ui.group.GroupScreen
import com.techvipin130524.smallcircle.ui.map.GeofenceMapScreen
import com.techvipin130524.smallcircle.ui.theme.SmallCircleTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy { InMemoryGroupRepository() }
    private val viewModel by viewModels<GroupViewModel> { GroupViewModelFactory(repository) }
    private val geofenceManager by lazy { GeofenceManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmallCircleTheme {
                val navController = rememberNavController()
                val activeGroup by viewModel.activeGroup.collectAsState()
                val alerts by viewModel.alerts.collectAsState()

                val startDestination = if (activeGroup != null) "dashboard" else "setup"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("setup") {
                        GroupScreen(
                            onCreateGroup = { groupName, userName ->
                                viewModel.createGroup(groupName, userName)
                                viewModel.activeGroup.value?.geofence?.let { geofence ->
                                    geofenceManager.addGeofence(geofence)
                                }
                                navController.navigate("dashboard") {
                                    popUpTo("setup") { inclusive = true }
                                }
                            },
                            onJoinGroup = { joinCode, userName ->
                                viewModel.joinGroup(joinCode, userName)
                                navController.navigate("dashboard") {
                                    popUpTo("setup") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("dashboard") {
                        val group = activeGroup
                        if (group != null) {
                            GroupDashboardScreen(
                                group = group,
                                alerts = alerts,
                                onOpenMap = { navController.navigate("map") },
                                onAddSampleMembers = { viewModel.addSampleMembers() },
                                onSimulateExit = { memberId -> viewModel.simulateMemberExit(memberId) }
                            )
                        } else {
                            navController.navigate("setup") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    }

                    composable("map") {
                        val group = activeGroup
                        if (group != null) {
                            GeofenceMapScreen(
                                group = group,
                                onUpdateGeofence = { updatedGeofence ->
                                    viewModel.updateGeofence(updatedGeofence)
                                    geofenceManager.addGeofence(updatedGeofence)
                                },
                                onSimulateMemberExit = { memberId -> viewModel.simulateMemberExit(memberId) },
                                onSimulateMemberEnter = { memberId -> viewModel.simulateMemberEnter(memberId) },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        } else {
                            navController.navigate("setup")
                        }
                    }
                }
            }
        }
    }
}
