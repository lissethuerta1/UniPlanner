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
import com.example.uniplanner.core.FragmentCommunicator

class TareasFragment : Fragment() {

    private var _binding: FragmentTareasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private val listaPendientes = mutableListOf<TareaModel>()
    private val listaCompletadas = mutableListOf<TareaModel>()
    private lateinit var communicator: FragmentCommunicator

    private var adapterPendientes: TareasAdapter? = null
    private var adapterCompletadas: TareasAdapter? = null
    private var materiaSeleccionadaActual= "Todas"

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

        binding.fabAddTarea.setOnClickListener {
            mostrarDialogoAgregarTarea()
        }

        // Escuchamos los datos de la API
        viewModel.homeDataState.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is ResponseService.Loading -> {
                    communicator.manageLoader(true)
                }
                is ResponseService.Success -> {
                    if (!viewModel.datosInicialesCargados) {
                        viewModel.listaPendientesGlobal.clear()
                        viewModel.listaPendientesGlobal.addAll(estado.data.tareas)
                        viewModel.listaCompletadasGlobal.clear()
                        viewModel.datosInicialesCargados = true
                    }
                    crearChipsDeMateriasDinamicos()
                    filtrarYActualizarTareas()

                    viewModel.actualizarContadorTareas(viewModel.listaPendientesGlobal.size)
                }

                is ResponseService.Error -> {
                    Toast.makeText(requireContext(), estado.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // NUEVA FUNCIÓN: Genera los Chips en caliente basándose en los datos reales
    private fun crearChipsDeMateriasDinamicos() {
        val todasLasTareas = viewModel.listaPendientesGlobal + viewModel.listaCompletadasGlobal
        // Obtenemos una lista de nombres de materias únicas y ordenadas
        val materiasUnicas = todasLasTareas.map { it.materia }.distinct().sorted()

        binding.chipGroupMaterias.removeAllViews()
        // Creamos siempre el chip por defecto para ver "Todas"
        val chipTodas = com.google.android.material.chip.Chip(requireContext()).apply {
            text = "Todas"
            isCheckable = true
            isChecked = (materiaSeleccionadaActual == "Todas")
            setOnClickListener {
                materiaSeleccionadaActual = "Todas"
                filtrarYActualizarTareas()
            }
        }
        binding.chipGroupMaterias.addView(chipTodas)

        // Creamos un chip dinámico por cada materia que encontramos
        materiasUnicas.forEach { nombreMateria ->
            if (nombreMateria.isNotEmpty()) {
                val nuevoChip = com.google.android.material.chip.Chip(requireContext()).apply {
                    text = nombreMateria
                    isCheckable = true
                    isChecked = (materiaSeleccionadaActual == nombreMateria)

                    // Al darle clic, actualizamos el filtro y refrescamos la lista
                    setOnClickListener {
                        materiaSeleccionadaActual = nombreMateria
                        filtrarYActualizarTareas()
                    }
                }
                binding.chipGroupMaterias.addView(nuevoChip)
            }
        }
    }

    private fun filtrarYActualizarTareas() {
        val pendientesFiltradas = if (materiaSeleccionadaActual == "Todas") {
            viewModel.listaPendientesGlobal
        } else {
            viewModel.listaPendientesGlobal.filter { it.materia.contains(materiaSeleccionadaActual, ignoreCase = true) }
        }

        // Filtrar Completadas
        val completadasFiltradas = if (materiaSeleccionadaActual == "Todas") {
            viewModel.listaCompletadasGlobal
        } else {
            viewModel.listaCompletadasGlobal.filter { it.materia.contains(materiaSeleccionadaActual, ignoreCase = true) }
        }

        //Si el RecyclerView de la vista no tiene adaptador, forzamos su creación inicial.
        if (binding.rvTareasPendientes.adapter == null || binding.rvTareasCompletadas.adapter == null) {
            setupAdaptersConListas(pendientesFiltradas, completadasFiltradas)
        } else {
            // Si la vista ya los tiene bien enlazados, solo refrescamos las listas
            adapterPendientes?.updateList(pendientesFiltradas)
            adapterCompletadas?.updateList(completadasFiltradas)
        }
    }

    private fun setupAdaptersConListas(pendientes: List<TareaModel>, completadas: List<TareaModel>) {
        adapterPendientes = TareasAdapter(pendientes.toMutableList(), false,
            onItemClick = { tarea -> irADetalle(tarea) },
            onCheckedChange = { tarea ->
                viewModel.listaPendientesGlobal.remove(tarea)
                viewModel.listaCompletadasGlobal.add(tarea)
                actualizarVistas()
            }
        )
        binding.rvTareasPendientes.adapter = adapterPendientes

        adapterCompletadas = TareasAdapter(completadas.toMutableList(), true,
            onItemClick = { tarea -> irADetalle(tarea) },
            onCheckedChange = { tarea ->
                viewModel.listaCompletadasGlobal.remove(tarea)
                viewModel.listaPendientesGlobal.add(tarea)
                actualizarVistas()
            }
        )
        binding.rvTareasCompletadas.adapter = adapterCompletadas
    }

    // Cada vez que se altere el dataset (Alta, Baja o Cambio de estado), recalculamos los chips
    private fun actualizarVistas() {
        crearChipsDeMateriasDinamicos()
        filtrarYActualizarTareas()
        viewModel.actualizarContadorTareas(viewModel.listaPendientesGlobal.size)
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

    private fun irADetalle(tarea: TareaModel) {
        val bundle = Bundle().apply {
            putSerializable("item_tarea", tarea)
        }
        findNavController().navigate(R.id.action_tareasFragment_to_detallesFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapterPendientes = null
        adapterCompletadas = null
    }
}