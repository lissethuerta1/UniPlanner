package com.example.uniplanner.home.materias
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.uniplanner.databinding.FragmentRegistroMateriaBinding
import com.example.uniplanner.home.HomeViewModel
class RegistroMateriaFragment : Fragment() {
    private var _binding: FragmentRegistroMateriaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroMateriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGuardarMateria.setOnClickListener {
            val nombre = binding.etMatNombre.text.toString().trim()
            val hora = binding.etMatHora.text.toString().trim()
            val salon = binding.etMatSalon.text.toString().trim()
            val dias = binding.etMatDias.text.toString().trim()

            if (nombre.isNotEmpty() && hora.isNotEmpty() && dias.isNotEmpty()) {

                viewModel.registrarNuevaMateria(nombre, hora, salon, dias)

                Toast.makeText(requireContext(), "Materia registrada en Firebase", Toast.LENGTH_SHORT).show()

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