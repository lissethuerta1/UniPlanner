package com.example.uniplanner.home.horario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.uniplanner.databinding.FragmentHorarioBinding

class HorarioFragment : Fragment() {

    private var _binding: FragmentHorarioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHorarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Detectar cuál día (Chip) de Material Design 3 se seleccionó
        binding.chipGroupDias.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedDay = when (checkedIds.first()) {
                    binding.chipLunes.id -> "Lunes"
                    binding.chipMartes.id -> "Martes"
                    binding.chipMiercoles.id -> "Miércoles"
                    binding.chipJueves.id -> "Jueves"
                    binding.chipViernes.id -> "Viernes"
                    else -> "Lunes"
                }
                // Aquí en el futuro se filtrara la información de la base de datos por el día elegido
                Toast.makeText(requireContext(), "Mostrando: $selectedDay", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}