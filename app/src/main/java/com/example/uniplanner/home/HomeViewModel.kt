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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val repository = TareasRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _homeDataState = MutableLiveData<ResponseService<ProyectResponse>>()
    val homeDataState: LiveData<ResponseService<ProyectResponse>> get() = _homeDataState

    private val _userProfileState = MutableLiveData<ResponseService<Map<String, Any>>>()
    val userProfileState: LiveData<ResponseService<Map<String, Any>>> get() = _userProfileState

    private val _contadorTareasReales = MutableLiveData<Int>(0)
    val contadorTareasReales: LiveData<Int> get() = _contadorTareasReales

    private val _contadorClasesHoy = MutableLiveData<Int>(0)
    val contadorClasesHoy: LiveData<Int> get() = _contadorClasesHoy

    val listaPendientesGlobal = mutableListOf<TareaModel>()
    val listaHorarioGlobal = mutableListOf<HorarioModel>()
    val listaCompletadasGlobal = mutableListOf<TareaModel>()
    var datosInicialesCargados = false

    fun actualizarContadorTareas(nuevoTotal: Int) {
        _contadorTareasReales.value = nuevoTotal
    }

    fun calcularClasesDeHoy(diaSemana: String) {
        val clasesHoy = listaHorarioGlobal.filter {
            it.dias.lowercase().contains(diaSemana.lowercase())
        }
        _contadorClasesHoy.value = clasesHoy.size
    }

    fun getUserProfile(uid: String) {
        _userProfileState.value = ResponseService.Loading
        viewModelScope.launch {
            try {
                val document = db.collection("users").document(uid).get().await()
                if (document != null && document.exists()) {
                    _userProfileState.postValue(ResponseService.Success(document.data ?: emptyMap()))
                } else {
                    _userProfileState.postValue(ResponseService.Error("El perfil no existe"))
                }
            } catch (e: Exception) {
                _userProfileState.postValue(ResponseService.Error(e.message ?: "Error al obtener datos"))
            }
        }
    }

    fun getHomeData() {
        _homeDataState.value = ResponseService.Loading

        viewModelScope.launch {
            try {
                val resultAPI = repository.fetchUniPlannerData()

                if (resultAPI is ResponseService.Success) {
                    val responseData = resultAPI.data

                    listaPendientesGlobal.clear()
                    listaHorarioGlobal.clear()
                    listaCompletadasGlobal.clear()

                    listaPendientesGlobal.addAll(responseData.tareas)
                    listaHorarioGlobal.addAll(responseData.horario)

                    // Carga de Tareas desde Firestore
                    val tareasSnapshot = db.collection("tareas").get().await()
                    for (document in tareasSnapshot.documents) {
                        val tareaFirebase = document.toObject(TareaModel::class.java)
                        if (tareaFirebase != null) {
                            if (!listaPendientesGlobal.any { it.idTarea == tareaFirebase.idTarea }) {
                                listaPendientesGlobal.add(0, tareaFirebase)
                            }
                        }
                    }

                    // Carga de Materias desde Firestore
                    val materiasSnapshot = db.collection("materias").get().await()
                    for (document in materiasSnapshot.documents) {
                        val materiaFirebase = document.toObject(HorarioModel::class.java)
                        if (materiaFirebase != null) {
                            if (!listaHorarioGlobal.any { it.idClase == materiaFirebase.idClase }) {
                                listaHorarioGlobal.add(materiaFirebase)
                            }
                        }
                    }

                    // Actualizamos contadores globales una vez unificadas las listas
                    actualizarContadorTareas(listaPendientesGlobal.size)
                    if (!datosInicialesCargados) {
                        calcularClasesDeHoy("Lunes")
                    }

                    datosInicialesCargados = true

                    val respuestaUnificada = responseData.copy(
                        tareas = listaPendientesGlobal,
                        horario = listaHorarioGlobal
                    )
                    _homeDataState.postValue(ResponseService.Success(respuestaUnificada))

                } else {
                    _homeDataState.postValue(resultAPI)
                }
            } catch (e: Exception) {
                _homeDataState.postValue(ResponseService.Error(e.message ?: "Error en la sincronización de datos"))
            }
        }
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

        val currentState = _homeDataState.value
        if (currentState is ResponseService.Success) {
            _homeDataState.postValue(ResponseService.Success(currentState.data.copy(tareas = ArrayList(listaPendientesGlobal))))
        }

        db.collection("tareas").document(idGenerado).set(nuevaTarea)
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

        val currentState = _homeDataState.value
        if (currentState is ResponseService.Success) {
            _homeDataState.postValue(ResponseService.Success(currentState.data.copy(horario = ArrayList(listaHorarioGlobal))))
        }

        db.collection("materias").document(idGenerado).set(nuevaMateria)
    }

    fun eliminarTareaDeFirebase(idTarea: String) {
        db.collection("tareas").document(idTarea)
            .delete()
            .addOnSuccessListener {
                listaPendientesGlobal.removeAll { it.idTarea == idTarea }
                listaCompletadasGlobal.removeAll { it.idTarea == idTarea }
                actualizarContadorTareas(listaPendientesGlobal.size)

                val currentState = _homeDataState.value
                if (currentState is ResponseService.Success) {
                    _homeDataState.postValue(ResponseService.Success(currentState.data.copy(tareas = ArrayList(listaPendientesGlobal))))
                }
            }
    }

    fun eliminarMateriaDeFirebase(idMateria: String) {
        db.collection("materias").document(idMateria)
            .delete()
            .addOnSuccessListener {
                listaHorarioGlobal.removeAll { it.idClase == idMateria }

                val currentState = _homeDataState.value
                if (currentState is ResponseService.Success) {
                    _homeDataState.postValue(ResponseService.Success(currentState.data.copy(horario = ArrayList(listaHorarioGlobal))))
                }
            }
    }
}