package com.example.uniplanner.core.network

import com.example.uniplanner.core.model.ProyectResponse
import com.example.uniplanner.core.ResponseService

interface TareasService {
    suspend fun fetchUniPlannerData(): ResponseService<ProyectResponse>
}
