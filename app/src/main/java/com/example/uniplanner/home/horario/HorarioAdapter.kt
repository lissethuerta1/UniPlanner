package com.example.uniplanner.home.horario

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.uniplanner.core.model.HorarioModel
import com.example.uniplanner.databinding.ItemMateriaPlaceholderDisenoBinding
class HorarioAdapter(private var listaClases: List<HorarioModel>) :
    RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder>() {

    class HorarioViewHolder(val binding: ItemMateriaPlaceholderDisenoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val binding = ItemMateriaPlaceholderDisenoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HorarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        val clase = listaClases[position]

        // Inyectamos los datos del JSON global
        holder.binding.tvNombreMateria.text = clase.materia
        holder.binding.tvHora.text = clase.hora
        holder.binding.tvSalonClase.text = clase.salon
    }

    override fun getItemCount(): Int = listaClases.size

    // Función para actualizar la lista cuando el usuario cambie de día
    fun updateList(nuevaLista: List<HorarioModel>) {
        this.listaClases = nuevaLista
        notifyDataSetChanged()
    }
}