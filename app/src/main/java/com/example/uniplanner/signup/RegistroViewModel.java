package com.example.uniplanner;

class RegistroViewModel: ViewModel() {

    private val authRepository = AuthRepository()

    private val _registerState = MutableStateFlow< ResponseService< FirebaseUser>?>(null)
}
