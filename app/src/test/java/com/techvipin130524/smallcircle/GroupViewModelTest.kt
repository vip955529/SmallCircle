package com.techvipin130524.smallcircle

import com.techvipin130524.smallcircle.data.InMemoryGroupRepository
import com.techvipin130524.smallcircle.model.GeofenceArea
import com.techvipin130524.smallcircle.model.GeofenceStatus
import com.techvipin130524.smallcircle.ui.GroupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: InMemoryGroupRepository
    private lateinit var viewModel: GroupViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = InMemoryGroupRepository()
        viewModel = GroupViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createGroup_setsActiveGroupAndCreatorAsMember() {
        viewModel.createGroup("Family Circle", "John")

        val group = viewModel.activeGroup.value
        assertNotNull(group)
        assertEquals("Family Circle", group?.name)
        assertEquals(1, group?.members?.size)

        val creator = group?.members?.first()
        assertEquals("John", creator?.name)
        assertTrue(creator?.isCurrentUser == true)
        assertEquals(GeofenceStatus.INSIDE, creator?.status)
    }

    @Test
    fun joinGroup_addsNewMemberToGroup() {
        val created = repository.createGroup("Trip Group", "Alice")
        viewModel.joinGroup(created.joinCode, "Bob")

        val group = viewModel.activeGroup.value
        assertNotNull(group)
        val members = group?.members ?: emptyList()
        assertTrue(members.any { it.name == "Bob" && it.isCurrentUser })
    }

    @Test
    fun simulateMemberExit_updatesStatusAndCreatesAlert() {
        viewModel.createGroup("Test Circle", "Alice")
        viewModel.addSampleMembers()

        val group = viewModel.activeGroup.value!!
        val sampleMember = group.members.first { !it.isCurrentUser }

        viewModel.simulateMemberExit(sampleMember.id)

        val updatedGroup = viewModel.activeGroup.value!!
        val updatedMember = updatedGroup.members.first { it.id == sampleMember.id }
        assertEquals(GeofenceStatus.OUTSIDE, updatedMember.status)

        val alerts = viewModel.alerts.value
        assertEquals(1, alerts.size)
        assertTrue(alerts.first().message.contains("${sampleMember.name} moved OUT"))
    }

    @Test
    fun updateGeofence_updatesGeofenceRegion() {
        viewModel.createGroup("Home Zone", "Charlie")
        val newGeofence = GeofenceArea(
            id = "g1",
            name = "New Safe Zone",
            latitude = 37.7749,
            longitude = -122.4194,
            radiusMeters = 500f
        )

        viewModel.updateGeofence(newGeofence)

        val updatedGroup = viewModel.activeGroup.value
        assertEquals("New Safe Zone", updatedGroup?.geofence?.name)
        assertEquals(500f, updatedGroup?.geofence?.radiusMeters)
    }
}
