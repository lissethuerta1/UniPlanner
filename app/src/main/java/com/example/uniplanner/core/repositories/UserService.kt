package com.example.uniplanner.core.repositories

import com.example.uniplanner.core.ResponseService
import com.example.uniplanner.onboarding.personal.model.UserProfile

interface UserService {

    suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit>
}