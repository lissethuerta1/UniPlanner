package com.example.uniplanner.core.network

import com.example.uniplanner.core.model.ProyectResponse
import retrofit2.Response
import retrofit2.http.GET
interface TareasAPI {
    @GET(".")
    suspend fun getUniPlannerData(): Response<ProyectResponse>
}