package com.techvipin130524.smallcircle.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    onCreateGroup: (groupName: String, userName: String) -> Unit,
    onJoinGroup: (joinCode: String, userName: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var joinCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmallCircle - Group Setup", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Geofenced Group Safety",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Create a tracking circle or join an existing one to get notified whenever a member leaves the geofence.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = userName,
                onValueChange = {
                    userName = it
                    errorMessage = null
                },
                label = { Text("Your Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        errorMessage = null
                    },
                    text = { Text("Create Group") },
                    icon = { Icon(Icons.Default.GroupAdd, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        errorMessage = null
                    },
                    text = { Text("Join Group") },
                    icon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (selectedTab == 0) {
                        Text(
                            text = "Create New Tracking Group",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = groupName,
                            onValueChange = {
                                groupName = it
                                errorMessage = null
                            },
                            label = { Text("Group Name (e.g. Family Circle)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (userName.isBlank()) {
                                    errorMessage = "Please enter your name"
                                } else if (groupName.isBlank()) {
                                    errorMessage = "Please enter a group name"
                                } else {
                                    onCreateGroup(groupName.trim(), userName.trim())
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create Group & Setup Geofence")
                        }
                    } else {
                        Text(
                            text = "Join Existing Group",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = joinCode,
                            onValueChange = {
                                joinCode = it
                                errorMessage = null
                            },
                            label = { Text("6-digit Group Code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (userName.isBlank()) {
                                    errorMessage = "Please enter your name"
                                } else if (joinCode.isBlank()) {
                                    errorMessage = "Please enter group join code"
                                } else {
                                    onJoinGroup(joinCode.trim(), userName.trim())
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Join Group")
                        }
                    }

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
