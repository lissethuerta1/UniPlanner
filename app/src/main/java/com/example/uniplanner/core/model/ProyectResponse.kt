package com.example.uniplanner.core.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

//MODELO PRINCIPAL
data class ProyectResponse(
    @SerializedName("dashboard") val dashboard: DashboardModel = DashboardModel(),
    @SerializedName("horario") val horario: List<HorarioModel> = emptyList(),
    @SerializedName("tareas") val tareas: List<TareaModel> = emptyList()
) : Serializable

data class DashboardModel(
    @SerializedName("tareas_pendientes_contador") val tareasPendientesContador: Int = 0,
    @SerializedName("clases_del_dia_contador") val clasesDelDiaContador: Int = 0,
    @SerializedName("frase_motivacional") val fraseMotivacional: String = ""
) : Serializable

data class HorarioModel(
    @SerializedName("idClase") var idClase: String = "",
    @SerializedName("materia") var materia: String = "",
    @SerializedName("hora") var hora: String = "",
    @SerializedName("salon") var salon: String = "",
    @SerializedName("dias") var dias: String = ""
) : Serializable

data class TareaModel(
    @SerializedName("id_tarea") val idTarea: String = "",
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("materia") val materia: String = "",
    @SerializedName("fecha_entrega") val fechaEntrega: String = "",
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("prioridad") val priority: String = "Normal"
) : Serializable