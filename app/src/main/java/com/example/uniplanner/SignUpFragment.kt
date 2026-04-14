package com.example.uniplanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.uniplanner.databinding.FragmentLoginBinding
import com.example.uniplanner.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {
        private var _binding: FragmentSignUpBinding? = null
        private val binding get() = _binding!!

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = FragmentSignUpBinding.inflate(inflater, container, false)

            binding.btnRegistrarse.setOnClickListener{
                findNavController().navigate(R.id.action_signUpFragment_to_registroFragment)
            }

            binding.btnBackSignup.setOnClickListener {
                findNavController().navigateUp()
            }

            return binding.root
        }
}

