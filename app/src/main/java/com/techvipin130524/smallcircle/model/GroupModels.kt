package com.techvipin130524.smallcircle.model

enum class GeofenceStatus {
    INSIDE,
    OUTSIDE,
    UNKNOWN
}

data class LocationUpdate(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class GeofenceArea(
    val id: String = "geofence_default",
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float
)

data class GroupMember(
    val id: String,
    val name: String,
    val isCurrentUser: Boolean = false,
    val currentLocation: LocationUpdate? = null,
    val status: GeofenceStatus = GeofenceStatus.INSIDE,
    val lastExitedAt: Long? = null
)

data class GeofenceAlert(
    val id: String,
    val memberId: String,
    val memberName: String,
    val groupName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TrackingGroup(
    val id: String,
    val name: String,
    val joinCode: String,
    val geofence: GeofenceArea?,
    val members: List<GroupMember> = emptyList()
)
