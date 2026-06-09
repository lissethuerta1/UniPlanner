package com.example.uniplanner.home.tareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.uniplanner.databinding.FragmentRegistroTareaBinding
import com.example.uniplanner.home.HomeViewModel
import androidx.fragment.app.Fragment
import android.R.attr.priority


class RegistroTareaFragment : Fragment() {
    private var _binding: FragmentRegistroTareaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroTareaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGuardarTarea.setOnClickListener {
            val titulo = binding.etRegTitulo.text.toString().trim()
            val materia = binding.etRegMateria.text.toString().trim()
            val fecha = binding.etRegFecha.text.toString().trim()
            val desc = binding.etRegDescripcion.text.toString().trim()
            val priority = binding.etRegPriority.text.toString().trim()

            if (titulo.isNotEmpty() && materia.isNotEmpty() && fecha.isNotEmpty() && priority.isNotEmpty()) {
                viewModel.agregarNuevaTarea(titulo, materia, fecha, desc, priority)

                Toast.makeText(requireContext(), "Guardado exitosamente", Toast.LENGTH_SHORT).show()

                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Por favor rellena los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}