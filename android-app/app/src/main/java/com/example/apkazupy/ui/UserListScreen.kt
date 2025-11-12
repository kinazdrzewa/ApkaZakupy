package com.example.apkazupy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apkazupy.data.User
import com.example.apkazupy.network.NetworkModule

@Composable
fun UserListScreen(authViewModel: AuthViewModel, onBack: () -> Unit) {
    val current = authViewModel.currentUser.collectAsState().value
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(current) {
        if (current != null && current.login == "admin") {
            loading = true
            try {
                users = NetworkModule.userApi.getAll()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        } else {
            error = "Dostęp zabroniony: wymagany admin"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Użytkownicy", style = MaterialTheme.typography.h6)
            Button(onClick = onBack) { Text("Powrót") }
        }

        if (loading) Text("Ładowanie...")
        error?.let { Text(it, color = MaterialTheme.colors.error) }

        if (!loading && error == null) {
            if (users.isEmpty()) Text("Brak użytkowników") else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(users) { u -> UserRow(u) }
                }
            }
        }
    }
}

@Composable
fun UserRow(u: User) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(u.login, style = MaterialTheme.typography.subtitle1)
        Text("id: ${u.id ?: "-"}")
    }
}
