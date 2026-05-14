package com.example.uniplanner.onboarding.personal

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.uniplanner.R
import com.example.uniplanner.core.FragmentCommunicator
import com.example.uniplanner.core.ResponseService
import com.example.uniplanner.databinding.FragmentPersonalInfoBinding
import com.example.uniplanner.home.HomeActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue

class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PersonalInfoViewModel>()
    private lateinit var communicator: FragmentCommunicator


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        communicator = requireActivity() as FragmentCommunicator
        setupValidation()
        setupDatePicker()
        setupClickListeners()
        observeState()
    }

    private fun setupValidation() {
        binding.btnGuardar.isEnabled = false
        binding.nombreText.addTextChangedListener { validateAndEnable() }
        binding.apellidosText.addTextChangedListener { validateAndEnable() }
        binding.fechaNText.addTextChangedListener { validateAndEnable() }
        binding.telefonoText.addTextChangedListener { validateAndEnable() }

    }

    private fun validateAndEnable() {
        val firstName = binding.nombreText.text.toString().trim()
        val lastName = binding.apellidosText.text.toString().trim()
        val birthDate = binding.fechaNText.text.toString().trim()
        val phone = binding.telefonoText.text.toString().trim()

        binding.nombreText.error = viewModel.validateFirstName(firstName)
        binding.apellidosText.error = viewModel.validateLastName(lastName)
        binding.fechaNText.error = viewModel.validateBirthDate(birthDate)
        binding.telefonoText.error = viewModel.validatePhone(phone)

        binding.btnGuardar.isEnabled =
            viewModel.isFormValid(firstName, lastName, birthDate, phone)
    }

    private fun setupDatePicker() {
        binding.fechaNText.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val formatted = "%04d-%02d-%02d".format(year, month + 1, day)
                    binding.fechaNText.setText(formatted)
                },
                cal.get(Calendar.YEAR) - 18,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
    }

    private fun setupClickListeners() {
        binding.btnGuardar.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Snackbar.make(binding.root, "Sesión inválida", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.saveProfile(
                uid = uid,
                firstName = binding.nombreText.text.toString().trim(),
                lastName = binding.apellidosText.text.toString().trim(),
                birthDate = binding.fechaNText.text.toString().trim(),
                phone = binding.telefonoText.text.toString().trim()
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnGuardar.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnGuardar.isEnabled = true
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        null -> Unit
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