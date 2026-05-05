package com.example.uniplanner.onboarding.signin

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.uniplanner.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import com.example.uniplanner.databinding.FragmentLoginBinding
import com.example.uniplanner.core.FragmentCommunicator
import androidx.lifecycle.repeatOnLifecycle
import com.example.uniplanner.core.ResponseService

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SignInViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator

        setupValidation()

        binding.textRegistrarse.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.textForgot.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_passwordFragment2)
        }

        binding.btnIngresar.setOnClickListener {
            val email = binding.emailText.text.toString().trim()
            val password = binding.passwordText.text.toString().trim()

            viewModel.requestLogin(email, password)
        }

        observeState()
        return binding.root
    }

    private fun setupValidation(){
        binding.btnIngresar.isEnabled = false
        binding.emailText.addTextChangedListener {
            validarFields()
        }
        binding.passwordText.addTextChangedListener {
            validarFields()
        }
    }

    private fun validarFields(){
        val email = binding.emailText.text.toString().trim()
        val password = binding.passwordText.text.toString().trim()

        val isEmailValid = isValidEmail(email)
        val isPasswordValid = password.length >= 8

        binding.emailText.error = if (email.isNotEmpty() && isEmailValid) null else "correo invalido"
        binding.passwordText.error = if (password.isNotEmpty() && isPasswordValid) null else "Minimo 8 caracteres"

        binding.btnIngresar.isEnabled =
            email.isNotEmpty() && password.isNotEmpty() && isEmailValid && isPasswordValid
    }

    private fun isValidEmail(email: String) : Boolean{
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnIngresar.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)

                            // navegar al home
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnIngresar.isEnabled = true

                            Snackbar.make(
                                binding.root,
                                state.error,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }
}