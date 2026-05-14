package com.example.uniplanner.home.tareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniplanner.databinding.FragmentTareasBinding
import com.example.uniplanner.home.tareas.model.Tarea
class TareasFragment : Fragment() {

    private var _binding: FragmentTareasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTareasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Crear datos de ejemplo para ver tus tarjetas premium en acción
        val listaUrgentes = listOf(
            Tarea("Entregable Unidad 2", "Administración de TI", "24 May", false),
            Tarea("Avance de Proyecto", "Estructura de Datos", "28 May", false)
        )

        val listaCompletadas = listOf(
            Tarea("Práctica 1 XML", "Diseño de Apps", "12 May", true)
        )

        // 2. Conectar las listas a sus respectivos RecyclerViews usando ViewBinding
        binding.rvTareasPendientes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTareasPendientes.adapter = TareasAdapter(listaUrgentes)

        binding.rvTareasCompletadas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTareasCompletadas.adapter = TareasAdapter(listaCompletadas)

        // Lógica para el botón flotante Material 3
        binding.fabAddTarea.setOnClickListener {
            Toast.makeText(requireContext(), "Próximamente: Crear Tarea", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}