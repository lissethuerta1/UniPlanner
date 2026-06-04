package com.example.uniplanner.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.uniplanner.databinding.FragmentDashboardBinding
import com.example.uniplanner.home.HomeViewModel
import androidx.fragment.app.activityViewModels
import com.example.uniplanner.core.ResponseService
import android.widget.Toast
import com.example.uniplanner.core.FragmentCommunicator

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var communicator: FragmentCommunicator

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Escuchamos el estado global de la API
        viewModel.homeDataState.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is ResponseService.Loading -> { }
                is ResponseService.Success -> {
                    val dashboardData = estado.data.dashboard

                    // Inyectamos los datos del JSON en tus vistas reales
                    binding.tvCountClases.text = dashboardData.clasesDelDiaContador.toString()
                    binding.tvDashboardSubtitle.text = dashboardData.fraseMotivacional
                }
                is ResponseService.Error -> {
                    Toast.makeText(requireContext(), estado.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.contadorTareasReales.observe(viewLifecycleOwner) { totalPendientes ->
            binding.tvCountTareas.text = totalPendientes.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}