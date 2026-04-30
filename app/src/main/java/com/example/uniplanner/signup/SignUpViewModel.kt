package com.example.uniplanner.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniplanner.core.AuthRepository
import kotlinx.coroutines.launch

class SignUpViewModel: ViewModel() {

    val repository = AuthRepository()

    fun requestSignUp(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.requestSignUp(email, password)
            result?.let { user ->
                //Log.i("Session", "Se ha creado el usuario ${user.uid}")
            } ?: run {
                Log.e("Error", "Hubo un error al crear al usuario")
            }
        }
    }
}