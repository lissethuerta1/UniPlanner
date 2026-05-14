package com.example.uniplanner.onboarding.signup

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
import com.example.uniplanner.databinding.FragmentSignUpBinding
import com.example.uniplanner.core.FragmentCommunicator
import com.example.uniplanner.core.ResponseService

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SignUpViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        communicator = requireActivity() as FragmentCommunicator
        setupClickListeners()
        observeState()
    }

    private fun setupValidation() {
        binding.btnBackSignup.isEnabled = false
        val watcher = { validateAndEnable() }
        binding.emailText.addTextChangedListener { validateAndEnable() }
        binding.passwordText.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val email = binding.emailText.text.toString().trim()
        val pass = binding.passwordText.text.toString().trim()

        binding.emailText.error = viewModel.validateEmail(email)
        binding.passwordText.error = viewModel.validatePassword(pass)

        binding.btnBackSignup.isEnabled =
            viewModel.isRegisterFormValid(email, pass)
    }

    private fun setupClickListeners() {
        binding.btnRegistrarse.setOnClickListener {
            val email = binding.emailText.text.toString().trim()
            val password = binding.passwordText.text.toString().trim()

            binding.btnRegistrarse.isEnabled = false
            viewModel.requestSignUp(email, password)
        }

        binding.btnBackSignup.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnRegistrarse.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            binding.btnRegistrarse.isEnabled = true
                            findNavController()
                                .navigate(R.id.action_signUpFragment_to_personalInfoFragment)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnRegistrarse.isEnabled = true

                            Snackbar.make(
                                binding.root,
                                state.error,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        null -> {
                            binding.btnRegistrarse.isEnabled = true
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