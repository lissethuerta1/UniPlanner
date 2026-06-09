package com.example.uniplanner.home.horario

import android.view.LayoutInflater
import android.os.Bundle
import com.example.uniplanner.R
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
        val clase = listaClases[position] // 🟢 Tu variable se llama 'clase'

        holder.binding.tvNombreMateria.text = clase.materia
        holder.binding.tvHora.text = clase.hora
        holder.binding.tvSalonClase.text = clase.salon

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply { putSerializable("item_materia", clase) }

            androidx.navigation.Navigation.findNavController(holder.itemView)
                .navigate(R.id.action_horarioFragment_to_detallesMateriaFragment, bundle)
        }
    }

    override fun getItemCount(): Int = listaClases.size

    // Función para actualizar la lista cuando el usuario cambie de día
    fun updateList(nuevaLista: List<HorarioModel>) {
        this.listaClases = nuevaLista
        notifyDataSetChanged()
    }
}