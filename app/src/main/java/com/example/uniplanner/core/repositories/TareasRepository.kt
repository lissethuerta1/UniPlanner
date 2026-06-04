package com.example.uniplanner.core.repository

import com.example.uniplanner.core.ResponseService
import com.example.uniplanner.core.model.ProyectResponse
import com.example.uniplanner.core.network.ApiClient
import com.example.uniplanner.core.network.TareasService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TareasRepository : TareasService {
    private val api = ApiClient.TareasApi

    override suspend fun fetchUniPlannerData(): ResponseService<ProyectResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getUniPlannerData()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ResponseService.Success(body)
                    } else {
                        ResponseService.Error("Respuesta vacía del servidor")
                    }
                } else {
                    ResponseService.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                ResponseService.Error(
                    "No se pudieron cargar los datos escolares: ${e.localizedMessage}"
                )
            }
        }
}