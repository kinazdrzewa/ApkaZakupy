package com.example.apkazupy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import com.example.apkazupy.ui.AppPrimary
import com.example.apkazupy.ui.AppOnPrimary
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(authViewModel: AuthViewModel, onAuthenticated: () -> Unit) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var mode by remember { mutableStateOf("login") } // or "register"

    val loading by authViewModel.loading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("foodscan", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(12.dp))

        // mode buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { mode = "login" }, enabled = mode != "login", colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)) { Text("Zaloguj") }
            Button(onClick = { mode = "register" }, enabled = mode != "register", colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)) { Text("Zarejestruj") }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Login") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Hasło") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        // client-side validation for registration
        val passwordValid = remember(password) {
            password.length >= 8 &&
            password.any { it.isLowerCase() } &&
            password.any { it.isUpperCase() } &&
            password.any { it.isDigit() }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (mode == "login") {
                Button(onClick = {
                    message = null
                    authViewModel.login(login, password) { ok, err ->
                        if (ok) onAuthenticated() else message = err ?: "Błąd"
                    }
                }, enabled = !loading, colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)) { Text("Zaloguj") }
            } else {
                Button(onClick = {
                    message = null
                    if (login.isBlank()) {
                        message = "Login nie może być pusty"
                        return@Button
                    }
                    if (!passwordValid) {
                        message = "Hasło musi mieć ≥8 znaków, małą i dużą literę oraz cyfrę"
                        return@Button
                    }
                    authViewModel.register(login, password) { ok, err ->
                        if (ok) onAuthenticated() else message = err ?: "Błąd rejestracji"
                    }
                }, enabled = !loading, colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)) { Text("Zarejestruj") }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Wymagania hasła: min 8 znaków, mała + duża litera, cyfra", style = MaterialTheme.typography.caption)

        if (loading) Text("Proszę czekać...")
        message?.let { Text(it, color = MaterialTheme.colors.error) }
    }
}
