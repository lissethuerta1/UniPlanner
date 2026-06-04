package com.example.uniplanner.home.tareas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.uniplanner.core.model.TareaModel
import com.example.uniplanner.databinding.ItemTareaPlaceholderDisenoBinding
class TareasAdapter(private var listaTareas: List<TareaModel>,
                    private val isCompletada: Boolean,
                    private val onItemClick: (TareaModel) -> Unit,
                    private val onCheckedChange: (TareaModel) -> Unit) :
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
        holder.binding.tvFechaTarea.text = tarea.fechaEntrega

        holder.binding.cbTarea.setOnCheckedChangeListener(null)
        holder.binding.cbTarea.isChecked = isCompletada

        // Escuchamos la interacción del usuario con el Checkbox
        holder.binding.cbTarea.setOnCheckedChangeListener { _, _ ->
            onCheckedChange(tarea)
        }

        holder.itemView.setOnClickListener {
            onItemClick(tarea)
        }
    }

    override fun getItemCount(): Int = listaTareas.size

    fun updateList(nuevaLista: List<TareaModel>) {
        this.listaTareas = nuevaLista
        notifyDataSetChanged()
    }
}