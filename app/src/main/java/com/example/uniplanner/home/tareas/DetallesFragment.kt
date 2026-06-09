package com.example.uniplanner.home.tareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.uniplanner.core.model.TareaModel
import com.example.uniplanner.databinding.FragmentDetallesBinding
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.uniplanner.home.HomeViewModel
import kotlin.getValue


class DetallesFragment : Fragment(){
    private val viewModel: HomeViewModel by activityViewModels()

    private var _binding: FragmentDetallesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetallesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tarea = arguments?.getSerializable("item_tarea") as? TareaModel

        tarea?.let {
            binding.tvDetailTitulo.text = it.titulo
            binding.tvDetailMateria.text = it.materia
            binding.tvDetailFecha.text = it.fechaEntrega
            binding.tvDetailDescripcion.text = it.descripcion
            binding.tvDetailPrioridad.text = "Prioridad: ${it.priority}"
        }

        binding.btnEliminar.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar Tarea?")
                .setMessage("Esta acción quitará la actividad de tu UniPlanner de forma permanente.")
                .setPositiveButton("Eliminar") { dialog, _ ->

                    // 1. Ejecutamos la baja en el ViewModel central
                    viewModel.eliminarTareaDeFirebase(tarea!!.idTarea)

                    Toast.makeText(requireContext(), "Tarea eliminada", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                    // 2. Regresamos automáticamente a la pantalla anterior (TareasFragment)
                    findNavController().popBackStack()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}