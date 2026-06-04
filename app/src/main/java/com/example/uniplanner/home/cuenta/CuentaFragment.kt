package com.example.uniplanner.home.cuenta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.uniplanner.databinding.FragmentCuentaBinding
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import kotlin.jvm.java
import com.example.uniplanner.home.HomeViewModel
import androidx.fragment.app.activityViewModels
import com.example.uniplanner.core.ResponseService
import android.widget.Toast
class CuentaFragment : Fragment() {
    private var _binding: FragmentCuentaBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCuentaBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            binding.tvProfileEmail.text = user.email

            //Disparamos la lectura a la base de datos usando el UID de la sesión activa
            viewModel.getUserProfile(user.uid)
        }

        // Observamos el estado de la base de datos de forma reactiva
        viewModel.userProfileState.observe(viewLifecycleOwner) { estado ->
            when (estado) {
                is ResponseService.Loading -> {
                    binding.tvProfileName.text = "Cargando..."
                }
                is ResponseService.Success -> {
                    val datosUsuario = estado.data

                    // Extraemos los campos exactamente con el mismo nombre que se guardaron
                    val nombre = datosUsuario["firstName"] as? String ?: ""
                    val apellido = datosUsuario["lastName"] as? String ?: ""

                    // Concatenamos el nombre y el apellido
                    if (nombre.isNotEmpty() || apellido.isNotEmpty()) {
                        binding.tvProfileName.text = "$nombre $apellido".trim()
                    } else {
                        binding.tvProfileName.text = "Alumno UniPlanner"
                    }
                }
                is ResponseService.Error -> {
                    // Si algo falla, dejamos un nombre por defecto
                    binding.tvProfileName.text = "Alumno UniPlanner"
                    Toast.makeText(requireContext(), estado.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Configuración del botón de Cerrar Sesión
        binding.btnLogout.setOnClickListener {
            auth.signOut()

            // Lo redirigimos de vuelta a la pantalla de Login para bloquear el acceso
            val intent = Intent(requireContext(), com.example.uniplanner.onboarding.MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}