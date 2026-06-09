package com.example.uniplanner.home.horario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.uniplanner.databinding.FragmentHorarioBinding
import com.example.uniplanner.home.HomeViewModel
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uniplanner.core.ResponseService
import com.example.uniplanner.core.model.HorarioModel
import androidx.navigation.fragment.findNavController
import com.example.uniplanner.R

class HorarioFragment : Fragment() {
    private var _binding: FragmentHorarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()
    private var horarioAdapter: HorarioAdapter? = null
    private var todasLasClases: List<HorarioModel> = emptyList()
    private var diaSeleccionadoActual = "Lunes"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHorarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvClasesHorario.layoutManager = LinearLayoutManager(requireContext())

        binding.fabAddMateria.setOnClickListener {
            findNavController().navigate(R.id.action_horarioFragment_to_registroMateriaFragment)
        }

        // Escuchamos la API
        viewModel.homeDataState.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is ResponseService.Loading -> {
                }
                is ResponseService.Success -> {
                    todasLasClases = viewModel.listaHorarioGlobal
                    actualizarListaPorDia()
                }
                is ResponseService.Error -> {
                    Toast.makeText(requireContext(), estado.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 4. Manejo de los Chips filtrados por día
        binding.chipGroupDias.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                diaSeleccionadoActual = when (checkedIds.first()) {
                    binding.chipLunes.id -> "Lunes"
                    binding.chipMartes.id -> "Martes"
                    binding.chipMiercoles.id -> "Miércoles"
                    binding.chipJueves.id -> "Jueves"
                    binding.chipViernes.id -> "Viernes"
                    else -> "Lunes"
                }
                actualizarListaPorDia()
            }
        }
    }

    // funcion para filtrar y actualizar
    private fun actualizarListaPorDia() {
        todasLasClases = viewModel.listaHorarioGlobal

        val clasesFiltradas = todasLasClases.filter {
            it.dias.contains(diaSeleccionadoActual, ignoreCase = true)
        }

        if (binding.rvClasesHorario.adapter == null) {
            horarioAdapter = HorarioAdapter(clasesFiltradas)
            binding.rvClasesHorario.adapter = horarioAdapter
        } else {
            horarioAdapter?.updateList(clasesFiltradas)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        horarioAdapter = null
    }
}