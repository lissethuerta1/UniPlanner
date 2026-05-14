package com.example.uniplanner.home.tareas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.uniplanner.home.tareas.model.Tarea
import com.example.uniplanner.databinding.ItemTareaPlaceholderDisenoBinding
class TareasAdapter(private val listaTareas: List<Tarea>) :
    RecyclerView.Adapter<TareasAdapter.TareaViewHolder>() {

    class TareaViewHolder(val binding: ItemTareaPlaceholderDisenoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val binding = ItemTareaPlaceholderDisenoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TareaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = listaTareas[position]

        // Inyectar la información en el XML
        holder.binding.tvNombreTarea.text = tarea.titulo
        holder.binding.tvMateriaTarea.text = tarea.materia
        holder.binding.tvFechaTarea.text = tarea.fecha
        holder.binding.cbTarea.isChecked = tarea.estaCompletada
    }

    override fun getItemCount(): Int = listaTareas.size
}