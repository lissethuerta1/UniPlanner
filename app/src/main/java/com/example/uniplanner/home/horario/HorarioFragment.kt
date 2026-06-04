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

class HorarioFragment : Fragment() {
    private var _binding: FragmentHorarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()
    private var horarioAdapter: HorarioAdapter? = null
    private var todasLasClases: List<HorarioModel> = emptyList()

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

        // Escuchamos la API
        viewModel.homeDataState.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is ResponseService.Loading -> {}
                is ResponseService.Success -> {
                    // Pasamos la lista de clases de internet al adaptador
                    todasLasClases = estado.data.horario

                    // Inicializamos el adaptador con el lunes por defecto
                    val clasesLunes = todasLasClases.filter { it.dias.contains("Lunes", ignoreCase = true) }
                    horarioAdapter = HorarioAdapter(clasesLunes)
                    binding.rvClasesHorario.adapter = horarioAdapter
                }

                is ResponseService.Error -> {
                    Toast.makeText(requireContext(), estado.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.chipGroupDias.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val diaSeleccionado = when (checkedIds.first()) {
                    binding.chipLunes.id -> "Lunes"
                    binding.chipMartes.id -> "Martes"
                    binding.chipMiercoles.id -> "Miércoles"
                    binding.chipJueves.id -> "Jueves"
                    binding.chipViernes.id -> "Viernes"
                    else -> "Lunes"
                }
                val clasesFiltradas = todasLasClases.filter { it.dias.contains(diaSeleccionado, ignoreCase = true) }
                horarioAdapter?.updateList(clasesFiltradas)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}