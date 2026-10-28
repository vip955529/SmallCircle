package com.techvipin130524.smallcircle.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.techvipin130524.smallcircle.data.GroupRepository
import com.techvipin130524.smallcircle.model.GeofenceAlert
import com.techvipin130524.smallcircle.model.GeofenceArea
import com.techvipin130524.smallcircle.model.TrackingGroup
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class GroupViewModel(
    private val repository: GroupRepository
) : ViewModel() {

    val activeGroup: StateFlow<TrackingGroup?> = repository.activeGroupFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val alerts: StateFlow<List<GeofenceAlert>> = repository.alertsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun createGroup(groupName: String, userName: String) {
        repository.createGroup(groupName, userName)
    }

    fun joinGroup(joinCode: String, userName: String) {
        repository.joinGroup(joinCode, userName)
    }

    fun updateGeofence(geofenceArea: GeofenceArea) {
        repository.updateGeofence(geofenceArea)
    }

    fun simulateMemberExit(memberId: String) {
        repository.simulateMemberExit(memberId)
    }

    fun simulateMemberEnter(memberId: String) {
        repository.simulateMemberEnter(memberId)
    }

    fun addSampleMembers() {
        repository.addSampleMembers()
    }
}

class GroupViewModelFactory(
    private val repository: GroupRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupViewModel(repository) as T
    }
}
