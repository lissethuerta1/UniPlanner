package com.example.uniplanner.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniplanner.core.ResponseService
import com.example.uniplanner.core.model.ProyectResponse
import com.example.uniplanner.core.repository.TareasRepository
import kotlinx.coroutines.launch
import com.example.uniplanner.core.model.TareaModel
import com.example.uniplanner.core.model.HorarioModel

class HomeViewModel : ViewModel() {

    private val repository = TareasRepository()
    private val _homeDataState = MutableLiveData<ResponseService<ProyectResponse>>()
    val homeDataState: LiveData<ResponseService<ProyectResponse>> get() = _homeDataState
    private val _userProfileState = MutableLiveData<ResponseService<Map<String, Any>>>()
    val userProfileState: LiveData<ResponseService<Map<String, Any>>> get() = _userProfileState
    private val _contadorTareasReales = MutableLiveData<Int>(0)
    val contadorTareasReales: LiveData<Int> get() = _contadorTareasReales
    private val _contadorClasesHoy = MutableLiveData<Int>(0)
    val contadorClasesHoy: LiveData<Int> get() = _contadorClasesHoy
    val listaPendientesGlobal = mutableListOf<TareaModel>()
    val listaCompletadasGlobal = mutableListOf<TareaModel>()
    val listaHorarioGlobal = mutableListOf<HorarioModel>()
    var datosInicialesCargados = false
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    fun actualizarContadorTareas(nuevoTotal: Int) {
        _contadorTareasReales.value = nuevoTotal
    }

    //Función para actualizar las clases del día actual
    fun calcularClasesDeHoy(diaSemana: String) {
        val clasesHoy = listaHorarioGlobal.filter { it.dias.contains(diaSemana, ignoreCase = true) }
        _contadorClasesHoy.value = clasesHoy.size
    }

    fun getUserProfile(uid: String) {
        _userProfileState.value = ResponseService.Loading
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    _userProfileState.postValue(ResponseService.Success(document.data ?: emptyMap()))
                } else {
                    _userProfileState.postValue(ResponseService.Error("El perfil no existe"))
                }
            }
            .addOnFailureListener { exception ->
                _userProfileState.postValue(ResponseService.Error(exception.message ?: "Error al obtener datos"))
            }
    }

    fun getHomeData() {
        _homeDataState.value = ResponseService.Loading

        viewModelScope.launch {
            val result = repository.fetchUniPlannerData()

            if (result is ResponseService.Success) {
                val responseData = result.data

                listaPendientesGlobal.clear()
                listaCompletadasGlobal.clear()
                listaHorarioGlobal.clear()

                listaPendientesGlobal.addAll(responseData.tareas)
                listaHorarioGlobal.addAll(responseData.horario)

                cargarTareasDesdeFirestore(responseData)
                cargarMateriasDesdeFirestore(responseData)

            } else {
                _homeDataState.postValue(result)
            }
        }
    }

    private fun cargarTareasDesdeFirestore(responseData: ProyectResponse) {
        db.collection("tareas")
            .get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    val tareaFirebase = document.toObject(TareaModel::class.java)
                    if (tareaFirebase != null) {
                        if (!listaPendientesGlobal.any { it.idTarea == tareaFirebase.idTarea }) {
                            listaPendientesGlobal.add(0, tareaFirebase)
                        }
                    }
                }
                actualizarContadorTareas(listaPendientesGlobal.size)
                notificarCambiosAUI(responseData)
            }
            .addOnFailureListener {
                notificarCambiosAUI(responseData)
            }
    }

    private fun cargarMateriasDesdeFirestore(responseData: ProyectResponse) {
        db.collection("materias")
            .get()
            .addOnSuccessListener { matSnapshot ->
                for (document in matSnapshot.documents) {
                    val materiaFirebase = document.toObject(HorarioModel::class.java)
                    if (materiaFirebase != null) {
                        if (!listaHorarioGlobal.any { it.idClase == materiaFirebase.idClase }) {
                            listaHorarioGlobal.add(materiaFirebase)
                        }
                    }
                }
                notificarCambiosAUI(responseData)
            }
            .addOnFailureListener {
                notificarCambiosAUI(responseData)
            }
    }

    private fun notificarCambiosAUI(responseData: ProyectResponse) {
        val respuestaUnificada = responseData.copy(
            tareas = listaPendientesGlobal,
            horario = listaHorarioGlobal
        )
        _homeDataState.postValue(ResponseService.Success(respuestaUnificada))
    }

    fun agregarNuevaTarea(titulo: String, materia: String, fecha: String, descripcion: String, priority: String) {
        val idGenerado = (System.currentTimeMillis() / 1000).toString()
        val nuevaTarea = TareaModel(
            idTarea = idGenerado,
            titulo = titulo,
            materia = materia,
            fechaEntrega = fecha,
            descripcion = descripcion,
            priority = priority
        )
        listaPendientesGlobal.add(0, nuevaTarea)
        actualizarContadorTareas(listaPendientesGlobal.size)

        db.collection("tareas")
            .document(idGenerado)
            .set(nuevaTarea)
    }

    fun registrarNuevaMateria(nombreMateria: String, hora: String, salon: String, dias: String) {
        val idGenerado = "CLASE_" + (System.currentTimeMillis() / 1000).toString()
        val nuevaMateria = HorarioModel(
            idClase = idGenerado,
            materia = nombreMateria,
            hora = hora,
            salon = salon,
            dias = dias
        )

        listaHorarioGlobal.add(nuevaMateria)

        db.collection("materias")
            .document(idGenerado)
            .set(nuevaMateria)
    }

    fun eliminarTareaDeFirebase(idTarea: String) {
        db.collection("tareas").document(idTarea)
            .delete()
            .addOnSuccessListener {
                listaPendientesGlobal.removeAll { it.idTarea == idTarea }
                listaCompletadasGlobal.removeAll { it.idTarea == idTarea }
                actualizarContadorTareas(listaPendientesGlobal.size)
            }
    }

    fun eliminarMateriaDeFirebase(idMateria: String) {
        db.collection("materias").document(idMateria)
            .delete()
            .addOnSuccessListener {
                listaHorarioGlobal.removeAll { it.idClase == idMateria }
            }
    }
}