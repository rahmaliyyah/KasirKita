package com.example.kasirkita.viewmodel

sealed class AuthCheckState {
    object Checking : AuthCheckState()
    object Authenticated : AuthCheckState()
    object NotAuthenticated : AuthCheckState()
}