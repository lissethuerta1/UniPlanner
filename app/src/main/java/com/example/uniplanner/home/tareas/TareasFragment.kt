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
    private val listaPendientes = mutableListOf<TareaModel>()
    private val listaCompletadas = mutableListOf<TareaModel>()

    private var adapterPendientes: TareasAdapter? = null
    private var adapterCompletadas: TareasAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTareasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Conectar las listas a sus respectivos RecyclerViews usando ViewBinding
        binding.rvTareasPendientes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTareasCompletadas.layoutManager = LinearLayoutManager(requireContext())


        // Escuchamos los datos de la API
        viewModel.homeDataState.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is ResponseService.Loading -> {}
                is ResponseService.Success -> {
                    if (!viewModel.datosInicialesCargados) {
                        viewModel.listaPendientesGlobal.clear()
                        viewModel.listaPendientesGlobal.addAll(estado.data.tareas)
                        viewModel.listaCompletadasGlobal.clear()
                        viewModel.datosInicialesCargados = true
                    }
                    setupAdapters()

                    viewModel.actualizarContadorTareas(listaPendientes.size)
                }

                is ResponseService.Error -> {
                    Toast.makeText(requireContext(), estado.error, Toast.LENGTH_LONG).show()
                }
            }
        }
        binding.fabAddTarea.setOnClickListener {
            mostrarDialogoAgregarTarea()
        }
    }

    private fun mostrarDialogoAgregarTarea() {
        // Creamos un contenedor inflable rápido para el formulario
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Nueva Tarea Premium")

        // Inflamos una vista vertical simple con campos de texto de manera dinámica
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etTitulo = android.widget.EditText(requireContext()).apply { hint = "Título de la tarea" }
        val etMateria = android.widget.EditText(requireContext()).apply { hint = "Materia (Ej. Informática)" }
        val etFecha = android.widget.EditText(requireContext()).apply { hint = "Fecha de entrega (Ej. 2026-06-15)" }
        val etDesc = android.widget.EditText(requireContext()).apply { hint = "Descripción detallada" }

        layout.addView(etTitulo)
        layout.addView(etMateria)
        layout.addView(etFecha)
        layout.addView(etDesc)
        builder.setView(layout)

        // Configuración de los botones del diálogo
        builder.setPositiveButton("Guardar") { dialog, _ ->
            val titulo = etTitulo.text.toString().trim()
            val materia = etMateria.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (titulo.isNotEmpty() && materia.isNotEmpty()) {
                // Mandamos los datos al CRUD del ViewModel
                viewModel.agregarNuevaTarea(titulo, materia, fecha, desc)

                // Refrescamos los adaptadores visuales al instante
                actualizarVistas()
                Toast.makeText(requireContext(), "Tarea agregada con éxito", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Por favor llena los campos principales", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun setupAdapters() {
        // Adaptador de Tareas Pendientes (Lee de las listas globales del ViewModel)
        adapterPendientes = TareasAdapter(viewModel.listaPendientesGlobal, false,
            onItemClick = { tarea -> irADetalle(tarea) },
            onCheckedChange = { tarea ->
                // Alteramos las listas del ViewModel
                viewModel.listaPendientesGlobal.remove(tarea)
                viewModel.listaCompletadasGlobal.add(tarea)
                actualizarVistas()
            }
        )
        binding.rvTareasPendientes.adapter = adapterPendientes

        // Adaptador de Tareas Completadas
        adapterCompletadas = TareasAdapter(viewModel.listaCompletadasGlobal, true,
            onItemClick = { tarea -> irADetalle(tarea) },
            onCheckedChange = { tarea ->
                viewModel.listaCompletadasGlobal.remove(tarea)
                viewModel.listaPendientesGlobal.add(tarea)
                actualizarVistas()
            }
        )
        binding.rvTareasCompletadas.adapter = adapterCompletadas
    }

    private fun actualizarVistas() {
        adapterPendientes?.updateList(viewModel.listaPendientesGlobal)
        adapterCompletadas?.updateList(viewModel.listaCompletadasGlobal)

        viewModel.actualizarContadorTareas(viewModel.listaPendientesGlobal.size)
    }

    private fun irADetalle(tarea: TareaModel) {
        val bundle = Bundle().apply {
            putSerializable("item_tarea", tarea)
        }
        findNavController().navigate(R.id.action_tareasFragment_to_detallesFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}