package com.example.androidapp.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val userId: String = "usr_ar_994",
    val username: String = "CyberPilot_AR",
    val level: Int = 8,
    val avatarUri: String = "avatar_cyber_pilot.png",
    val isAuthenticated: Boolean = true
)

/**
 * Profile / Auth Manager (Persistent Core).
 * Manages player authentication token, profile state, and global player level.
 */
class ProfileManager {
    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    fun getUserId(): String = _profile.value.userId
    fun isUserLoggedIn(): Boolean = _profile.value.isAuthenticated
}
