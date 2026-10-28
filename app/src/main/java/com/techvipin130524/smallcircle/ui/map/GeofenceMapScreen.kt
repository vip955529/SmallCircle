package com.techvipin130524.smallcircle.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.techvipin130524.smallcircle.model.GeofenceArea
import com.techvipin130524.smallcircle.model.GeofenceStatus
import com.techvipin130524.smallcircle.model.GroupMember
import com.techvipin130524.smallcircle.model.TrackingGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceMapScreen(
    group: TrackingGroup,
    onUpdateGeofence: (GeofenceArea) -> Unit,
    onSimulateMemberExit: (String) -> Unit,
    onSimulateMemberEnter: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val geofence = group.geofence ?: GeofenceArea(
        name = "${group.name} Geofence",
        latitude = 37.7749,
        longitude = -122.4194,
        radiusMeters = 200f
    )

    var centerLat by remember { mutableStateOf(geofence.latitude) }
    var centerLng by remember { mutableStateOf(geofence.longitude) }
    var radius by remember { mutableFloatStateOf(geofence.radiusMeters) }
    var geofenceName by remember { mutableStateOf(geofence.name) }

    val initialLatLng = LatLng(centerLat, centerLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 15f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Geofence Config & Map") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        centerLat = latLng.latitude
                        centerLng = latLng.longitude
                    }
                ) {
                    val geofenceCenter = LatLng(centerLat, centerLng)
                    Circle(
                        center = geofenceCenter,
                        radius = radius.toDouble(),
                        strokeColor = Color(0xFF0066FF),
                        strokeWidth = 4f,
                        fillColor = Color(0x330066FF)
                    )

                    Marker(
                        state = MarkerState(position = geofenceCenter),
                        title = "Geofence Center",
                        snippet = geofenceName
                    )

                    group.members.forEach { member ->
                        val location = member.currentLocation
                        if (location != null) {
                            val memberLatLng = LatLng(location.latitude, location.longitude)
                            Marker(
                                state = MarkerState(position = memberLatLng),
                                title = member.name + if (member.isCurrentUser) " (You)" else "",
                                snippet = "Status: ${member.status}"
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Configure Geofence Area",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = geofenceName,
                    onValueChange = { geofenceName = it },
                    label = { Text("Geofence Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Radius: ${radius.toInt()} meters",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = radius,
                    onValueChange = { radius = it },
                    valueRange = 50f..1000f,
                    steps = 19
                )

                Button(
                    onClick = {
                        val updated = GeofenceArea(
                            id = geofence.id,
                            name = geofenceName,
                            latitude = centerLat,
                            longitude = centerLng,
                            radiusMeters = radius
                        )
                        onUpdateGeofence(updated)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Geofence Region")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Simulate Member Movements",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                group.members.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.name + if (member.isCurrentUser) " (You)" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (member.status == GeofenceStatus.INSIDE) {
                            OutlinedButton(
                                onClick = { onSimulateMemberExit(member.id) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simulate Exit")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSimulateMemberEnter(member.id) }
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Re-enter Geofence")
                            }
                        }
                    }
                }
            }
        }
    }
}
