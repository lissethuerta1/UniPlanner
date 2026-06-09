package com.example.uniplanner.home.materias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.uniplanner.core.model.HorarioModel
import com.example.uniplanner.databinding.FragmentDetallesMateriaBinding
import com.example.uniplanner.home.HomeViewModel

class DetallesMateriaFragment : Fragment() {

    private var _binding: FragmentDetallesMateriaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private var materiaSeleccionada: HorarioModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetallesMateriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        materiaSeleccionada = arguments?.getSerializable("item_materia") as? HorarioModel

        materiaSeleccionada?.let { materia ->
            binding.tvDetalleMatNombre.text = materia.materia
            binding.tvDetalleMatHora.text = materia.hora
            binding.tvDetalleMatSalon.text = materia.salon
            binding.tvDetalleMatDias.text = materia.dias

            binding.btnEliminarMateria.setOnClickListener {
                viewModel.eliminarMateriaDeFirebase(materia.idClase)
                Toast.makeText(requireContext(), "Materia eliminada", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        } ?: run {
            Toast.makeText(requireContext(), "Error al cargar la asignatura", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}