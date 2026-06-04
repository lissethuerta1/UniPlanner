package com.example.uniplanner.core.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// 1. MODELO PRINCIPAL
data class ProyectResponse(
    @SerializedName("dashboard") val dashboard: DashboardModel,
    @SerializedName("horario") val horario: List<HorarioModel>,
    @SerializedName("tareas") val tareas: List<TareaModel>
) : Serializable

// 2. SUBMODELO: Dashboard
data class DashboardModel(
    @SerializedName("tareas_pendientes_contador") val tareasPendientesContador: Int,
    @SerializedName("clases_del_dia_contador") val clasesDelDiaContador: Int,
    @SerializedName("frase_motivacional") val fraseMotivacional: String
) : Serializable

// 3. SUBMODELO: Horario
data class HorarioModel(
    @SerializedName("id_clase") val idClase: String,
    @SerializedName("materia") val materia: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("salon") val salon: String,
    @SerializedName("dias") val dias: String
) : Serializable

// 4. SUBMODELO: Tareas
data class TareaModel(
    @SerializedName("id_tarea") val idTarea: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("materia") val materia: String,
    @SerializedName("fecha_entrega") val fechaEntrega: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("prioridad") val priority: String
) : Serializable