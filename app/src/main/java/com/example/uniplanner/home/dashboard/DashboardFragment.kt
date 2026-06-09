package com.example.uniplanner.home.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.uniplanner.core.ResponseService
import com.example.uniplanner.databinding.FragmentDashboardBinding
import com.example.uniplanner.home.HomeViewModel

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
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

        viewModel.homeDataState.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is ResponseService.Loading -> { }
                is ResponseService.Success -> {
                    val dashboardData = estado.data.dashboard
                    binding.tvDashboardSubtitle.text = dashboardData.fraseMotivacional
                }
                is ResponseService.Error -> {
                    Toast.makeText(requireContext(), estado.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        //Observar el contador real de tareas pendientes
        viewModel.contadorTareasReales.observe(viewLifecycleOwner) { totalPendientes ->
            binding.tvCountTareas.text = totalPendientes.toString()
        }

        //Calcular qué día es hoy para filtrar el horario real
        val sdf = java.text.SimpleDateFormat("EEEE", java.util.Locale("es", "MX"))
        val diaDeHoy = sdf.format(java.util.Date()).replaceFirstChar { it.uppercase() }

        viewModel.calcularClasesDeHoy(diaDeHoy)

        //Observar el contador real de clases del día
        viewModel.contadorClasesHoy.observe(viewLifecycleOwner) { totalClases ->
            binding.tvCountClases.text = totalClases.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}