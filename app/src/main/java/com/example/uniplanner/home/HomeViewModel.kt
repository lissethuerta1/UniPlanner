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
import android.R.attr.priority

class HomeViewModel : ViewModel() {

    private val repository = TareasRepository()
    private val _homeDataState = MutableLiveData<ResponseService<ProyectResponse>>()
    val homeDataState: LiveData<ResponseService<ProyectResponse>> get() = _homeDataState
    private val _userProfileState = MutableLiveData<ResponseService<Map<String, Any>>>()
    val userProfileState: LiveData<ResponseService<Map<String, Any>>> get() = _userProfileState
    // LiveData para controlar el número real de tareas pendientes
    private val _contadorTareasReales = MutableLiveData<Int>(0)
    val contadorTareasReales: LiveData<Int> get() = _contadorTareasReales
    val listaPendientesGlobal = mutableListOf<TareaModel>()
    val listaCompletadasGlobal = mutableListOf<TareaModel>()
    var datosInicialesCargados = false

    fun actualizarContadorTareas(nuevoTotal: Int) {
        _contadorTareasReales.value = nuevoTotal
    }
    fun getUserProfile(uid: String) {
        _userProfileState.value = ResponseService.Loading
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
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
            _homeDataState.postValue(result)
        }
    }

    // C: CREAR - Agrega una nueva tarea hecha por el alumno
    fun agregarNuevaTarea(titulo: String, materia: String, fecha: String, descripcion: String) {
        val nuevaTarea = TareaModel(
            idTarea = (System.currentTimeMillis() / 1000).toString(),
            titulo = titulo,
            materia = materia,
            fechaEntrega = fecha,
            descripcion = descripcion,
            priority = "Normal"
        )
        listaPendientesGlobal.add(0, nuevaTarea)
        actualizarContadorTareas(listaPendientesGlobal.size)
    }

    //ELIMINAR - Borra la tarea de cualquier lista en la que se encuentre
    fun eliminarTarea(tarea: TareaModel) {
        listaPendientesGlobal.remove(tarea)
        listaCompletadasGlobal.remove(tarea)

        // Sincronizamos el contador
        actualizarContadorTareas(listaPendientesGlobal.size)
    }

}