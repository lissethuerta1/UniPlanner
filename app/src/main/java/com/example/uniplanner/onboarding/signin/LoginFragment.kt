package com.example.uniplanner.onboarding.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.uniplanner.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import com.example.uniplanner.databinding.FragmentLoginBinding
import com.example.uniplanner.core.FragmentCommunicator
import com.example.uniplanner.core.ResponseService
import android.content.Intent
import com.example.uniplanner.home.HomeActivity

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SignInViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        communicator = requireActivity() as FragmentCommunicator
        setupValidation()
        setupClickListeners()
        observeState()
    }

    private fun setupValidation() {
        binding.btnIngresar.isEnabled = false
        binding.emailText.addTextChangedListener { validateAndEnable() }
        binding.passwordText.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val email = binding.emailText.text.toString().trim()
        val password = binding.passwordText.text.toString().trim()

        binding.emailText.error = viewModel.validateEmail(email)
        binding.passwordText.error = viewModel.validatePassword(password)
        binding.btnIngresar.isEnabled = viewModel.isLoginFormValid(email, password)
    }

    private fun setupClickListeners() {
        binding.textRegistrarse.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.textForgot.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_passwordFragment2)
        }

        binding.btnIngresar.setOnClickListener {
            cleanErrors()

            val email = binding.emailText.text.toString().trim()
            val password = binding.passwordText.text.toString().trim()

            val emailError = viewModel.validateEmail(email)
            val passwordError = viewModel.validatePassword(password)

            if (emailError == null && passwordError == null) {
                binding.btnIngresar.isEnabled = false
                viewModel.requestLogin(email, password)
            } else {
                if (emailError != null) binding.emailText.error = emailError
                if (passwordError != null) binding.passwordText.error = passwordError
            }
        }
    }

    private fun cleanErrors() {
        binding.emailText.error = null
        binding.passwordText.error = null
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnIngresar.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            binding.btnIngresar.isEnabled = true

                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
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
                        null -> {
                            binding.btnIngresar.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}