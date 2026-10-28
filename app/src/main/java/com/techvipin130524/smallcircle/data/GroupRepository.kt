package com.techvipin130524.smallcircle.data

import com.techvipin130524.smallcircle.model.GeofenceAlert
import com.techvipin130524.smallcircle.model.GeofenceArea
import com.techvipin130524.smallcircle.model.GeofenceStatus
import com.techvipin130524.smallcircle.model.GroupMember
import com.techvipin130524.smallcircle.model.LocationUpdate
import com.techvipin130524.smallcircle.model.TrackingGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

interface GroupRepository {
    val activeGroupFlow: Flow<TrackingGroup?>
    val alertsFlow: Flow<List<GeofenceAlert>>

    fun createGroup(groupName: String, userName: String): TrackingGroup
    fun joinGroup(joinCode: String, userName: String): TrackingGroup?
    fun updateGeofence(geofence: GeofenceArea)
    fun updateUserLocation(latitude: Double, longitude: Double)
    fun simulateMemberExit(memberId: String)
    fun simulateMemberEnter(memberId: String)
    fun recordGeofenceExitAlert(memberId: String, memberName: String, groupName: String): GeofenceAlert
    fun addSampleMembers()
}

class InMemoryGroupRepository : GroupRepository {

    private val _activeGroup = MutableStateFlow<TrackingGroup?>(null)
    override val activeGroupFlow: Flow<TrackingGroup?> = _activeGroup.asStateFlow()

    private val _alerts = MutableStateFlow<List<GeofenceAlert>>(emptyList())
    override val alertsFlow: Flow<List<GeofenceAlert>> = _alerts.asStateFlow()

    override fun createGroup(groupName: String, userName: String): TrackingGroup {
        val joinCode = (100000..999999).random().toString()
        val currentUserId = UUID.randomUUID().toString()
        val currentUser = GroupMember(
            id = currentUserId,
            name = userName,
            isCurrentUser = true,
            currentLocation = LocationUpdate(37.7749, -122.4194),
            status = GeofenceStatus.INSIDE
        )

        val defaultGeofence = GeofenceArea(
            id = "geo_${UUID.randomUUID().toString().take(8)}",
            name = "$groupName Safe Zone",
            latitude = 37.7749,
            longitude = -122.4194,
            radiusMeters = 200f
        )

        val group = TrackingGroup(
            id = UUID.randomUUID().toString(),
            name = groupName,
            joinCode = joinCode,
            geofence = defaultGeofence,
            members = listOf(currentUser)
        )

        _activeGroup.value = group
        return group
    }

    override fun joinGroup(joinCode: String, userName: String): TrackingGroup? {
        val currentGroup = _activeGroup.value
        val groupToJoin = if (currentGroup != null && currentGroup.joinCode == joinCode) {
            currentGroup
        } else {
            // Create a mock group for demo joining
            TrackingGroup(
                id = UUID.randomUUID().toString(),
                name = "Circle $joinCode",
                joinCode = joinCode,
                geofence = GeofenceArea(
                    id = "geo_join",
                    name = "Group Home Zone",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    radiusMeters = 150f
                ),
                members = listOf(
                    GroupMember("m1", "Alice", isCurrentUser = false, LocationUpdate(37.7750, -122.4190), GeofenceStatus.INSIDE),
                    GroupMember("m2", "Bob", isCurrentUser = false, LocationUpdate(37.7745, -122.4198), GeofenceStatus.INSIDE)
                )
            )
        }

        val newUser = GroupMember(
            id = UUID.randomUUID().toString(),
            name = userName,
            isCurrentUser = true,
            currentLocation = LocationUpdate(37.7749, -122.4194),
            status = GeofenceStatus.INSIDE
        )

        val updatedMembers = groupToJoin.members.filterNot { it.isCurrentUser } + newUser
        val updatedGroup = groupToJoin.copy(members = updatedMembers)
        _activeGroup.value = updatedGroup
        return updatedGroup
    }

    override fun updateGeofence(geofence: GeofenceArea) {
        _activeGroup.update { group ->
            group?.copy(geofence = geofence)
        }
    }

    override fun updateUserLocation(latitude: Double, longitude: Double) {
        _activeGroup.update { group ->
            if (group == null) return@update null
            val updatedMembers = group.members.map { member ->
                if (member.isCurrentUser) {
                    val isInside = group.geofence?.let {
                        isLocationInsideGeofence(latitude, longitude, it.latitude, it.longitude, it.radiusMeters)
                    } ?: true

                    val newStatus = if (isInside) GeofenceStatus.INSIDE else GeofenceStatus.OUTSIDE
                    if (member.status == GeofenceStatus.INSIDE && newStatus == GeofenceStatus.OUTSIDE) {
                        recordGeofenceExitAlert(member.id, member.name, group.name)
                    }

                    member.copy(
                        currentLocation = LocationUpdate(latitude, longitude),
                        status = newStatus,
                        lastExitedAt = if (newStatus == GeofenceStatus.OUTSIDE) System.currentTimeMillis() else member.lastExitedAt
                    )
                } else member
            }
            group.copy(members = updatedMembers)
        }
    }

    override fun simulateMemberExit(memberId: String) {
        _activeGroup.update { group ->
            if (group == null) return@update null
            var exitedMemberName: String? = null
            val updatedMembers = group.members.map { member ->
                if (member.id == memberId) {
                    exitedMemberName = member.name
                    // Shift coordinate away outside geofence radius
                    val offsetLat = (group.geofence?.latitude ?: 37.7749) + 0.005
                    val offsetLng = (group.geofence?.longitude ?: -122.4194) + 0.005
                    member.copy(
                        currentLocation = LocationUpdate(offsetLat, offsetLng),
                        status = GeofenceStatus.OUTSIDE,
                        lastExitedAt = System.currentTimeMillis()
                    )
                } else member
            }

            exitedMemberName?.let { name ->
                recordGeofenceExitAlert(memberId, name, group.name)
            }

            group.copy(members = updatedMembers)
        }
    }

    override fun simulateMemberEnter(memberId: String) {
        _activeGroup.update { group ->
            if (group == null) return@update null
            val updatedMembers = group.members.map { member ->
                if (member.id == memberId) {
                    val centerLat = group.geofence?.latitude ?: 37.7749
                    val centerLng = group.geofence?.longitude ?: -122.4194
                    member.copy(
                        currentLocation = LocationUpdate(centerLat, centerLng),
                        status = GeofenceStatus.INSIDE
                    )
                } else member
            }
            group.copy(members = updatedMembers)
        }
    }

    override fun recordGeofenceExitAlert(
        memberId: String,
        memberName: String,
        groupName: String
    ): GeofenceAlert {
        val alert = GeofenceAlert(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            memberName = memberName,
            groupName = groupName,
            message = "$memberName moved OUT of the $groupName geofenced region!"
        )
        _alerts.update { current -> listOf(alert) + current }
        return alert
    }

    override fun addSampleMembers() {
        _activeGroup.update { group ->
            if (group == null) return@update null
            val centerLat = group.geofence?.latitude ?: 37.7749
            val centerLng = group.geofence?.longitude ?: -122.4194
            val sampleMembers = listOf(
                GroupMember("s1", "Sarah Connor", false, LocationUpdate(centerLat + 0.0005, centerLng + 0.0005), GeofenceStatus.INSIDE),
                GroupMember("s2", "John Doe", false, LocationUpdate(centerLat - 0.0008, centerLng - 0.0004), GeofenceStatus.INSIDE)
            )
            val existingIds = group.members.map { it.id }.toSet()
            val newMembers = sampleMembers.filterNot { it.id in existingIds }
            group.copy(members = group.members + newMembers)
        }
    }

    private fun isLocationInsideGeofence(
        lat: Double,
        lng: Double,
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Float
    ): Boolean {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat, lng, centerLat, centerLng, results)
        return results[0] <= radiusMeters
    }
}
