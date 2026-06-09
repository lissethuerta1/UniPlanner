package com.example.uniplanner.home.tareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniplanner.databinding.FragmentTareasBinding
import com.example.uniplanner.core.ResponseService
import com.example.uniplanner.home.HomeViewModel
import com.example.uniplanner.R
import androidx.navigation.fragment.findNavController
import com.example.uniplanner.core.model.TareaModel

class TareasFragment : Fragment() {
    private var _binding: FragmentTareasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private var adapterPendientes: TareasAdapter? = null
    private var adapterCompletadas: TareasAdapter? = null
    private var materiaSeleccionadaActual = "Todas"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTareasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvTareasPendientes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTareasCompletadas.layoutManager = LinearLayoutManager(requireContext())

        binding.fabAddTarea.setOnClickListener {
            findNavController().navigate(R.id.action_tareasFragment_to_registroTareaFragment)
        }

        //Observador del estado de datos
        viewModel.homeDataState.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is ResponseService.Loading -> {
                }
                is ResponseService.Success -> {
                    actualizarVistas()
                }
                is ResponseService.Error -> {
                    Toast.makeText(requireContext(), estado.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Procesa y genera los Chips basados en el listado real
    private fun crearChipsDeMateriasDinamicos() {
        val todasLasTareas = viewModel.listaPendientesGlobal + viewModel.listaCompletadasGlobal
        val materiasUnicas = todasLasTareas.map { it.materia }.distinct().sorted()

        binding.chipGroupMaterias.removeAllViews()

        agregarChipMateria("Todas")

        materiasUnicas.filter { it.isNotEmpty() }.forEach { agregarChipMateria(it) }
    }

    private fun agregarChipMateria(nombre: String) {
        val nuevoChip = com.google.android.material.chip.Chip(requireContext()).apply {
            text = nombre
            isCheckable = true
            isChecked = (materiaSeleccionadaActual == nombre)
            setOnClickListener {
                materiaSeleccionadaActual = nombre
                filtrarYActualizarTareas()
            }
        }
        binding.chipGroupMaterias.addView(nuevoChip)
    }

    private fun filtrarYActualizarTareas() {
        val filtrar: (List<TareaModel>) -> List<TareaModel> = { lista ->
            if (materiaSeleccionadaActual == "Todas") lista
            else lista.filter { it.materia.contains(materiaSeleccionadaActual, ignoreCase = true) }
        }

        val pendientesFiltradas = filtrar(viewModel.listaPendientesGlobal)
        val completadasFiltradas = filtrar(viewModel.listaCompletadasGlobal)

        if (binding.rvTareasPendientes.adapter == null || binding.rvTareasCompletadas.adapter == null) {
            setupAdapters(pendientesFiltradas, completadasFiltradas)
        } else {
            adapterPendientes?.updateList(pendientesFiltradas)
            adapterCompletadas?.updateList(completadasFiltradas)
        }
    }

    private fun setupAdapters(pendientes: List<TareaModel>, completadas: List<TareaModel>) {
        adapterPendientes = TareasAdapter(pendientes.toMutableList(), false,
            onItemClick = { irADetalle(it) },
            onCheckedChange = { tarea ->
                viewModel.listaPendientesGlobal.remove(tarea)
                viewModel.listaCompletadasGlobal.add(tarea)
                actualizarVistas()
            }
        )
        binding.rvTareasPendientes.adapter = adapterPendientes

        adapterCompletadas = TareasAdapter(completadas.toMutableList(), true,
            onItemClick = { irADetalle(it) },
            onCheckedChange = { tarea ->
                viewModel.listaCompletadasGlobal.remove(tarea)
                viewModel.listaPendientesGlobal.add(tarea)
                actualizarVistas()
            }
        )
        binding.rvTareasCompletadas.adapter = adapterCompletadas
    }

    private fun actualizarVistas() {
        crearChipsDeMateriasDinamicos()
        filtrarYActualizarTareas()
        viewModel.actualizarContadorTareas(viewModel.listaPendientesGlobal.size)
    }

    private fun irADetalle(tarea: TareaModel) {
        val bundle = Bundle().apply { putSerializable("item_tarea", tarea) }
        findNavController().navigate(R.id.action_tareasFragment_to_detallesFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapterPendientes = null
        adapterCompletadas = null
    }
}