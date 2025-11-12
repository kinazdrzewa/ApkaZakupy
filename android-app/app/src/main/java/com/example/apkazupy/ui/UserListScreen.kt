package com.example.apkazupy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import com.example.apkazupy.ui.AppPrimary
import com.example.apkazupy.ui.AppOnPrimary
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
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

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(scaffoldState = scaffoldState) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Użytkownicy", style = MaterialTheme.typography.h6, color = AppPrimary)
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)) { Text("Powrót") }
            }

            if (loading) Text("Ładowanie...")
            error?.let { Text(it, color = MaterialTheme.colors.error) }

            if (!loading && error == null) {
                if (users.isEmpty()) Text("Brak użytkowników") else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(users) { u ->
                            var showDeleteConfirm by remember { mutableStateOf(false) }
                            UserRow(u, onDeleteClick = { showDeleteConfirm = true })

                            if (showDeleteConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirm = false },
                                    title = { Text("Usuń użytkownika") },
                                    text = { Text("Czy na pewno chcesz usunąć użytkownika '${u.login}'?") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            // perform delete
                                            scope.launch {
                                                try {
                                                    val resp = NetworkModule.userApi.delete(u.id ?: -1L)
                                                    if (resp.isSuccessful) {
                                                        users = NetworkModule.userApi.getAll()
                                                        scaffoldState.snackbarHostState.showSnackbar("Użytkownik usunięty")
                                                    } else {
                                                        scaffoldState.snackbarHostState.showSnackbar("Błąd usuwania: ${resp.code()}")
                                                    }
                                                } catch (e: Exception) {
                                                    scaffoldState.snackbarHostState.showSnackbar("Błąd: ${e.message}")
                                                }
                                                showDeleteConfirm = false
                                            }
                                        }) { Text("Usuń", color = AppPrimary) }
                                    },
                                    dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Anuluj") } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserRow(u: User, onDeleteClick: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(u.login, style = MaterialTheme.typography.subtitle1)
                Text("id: ${u.id ?: "-"}")
            }
            if (u.login != "admin") {
                IconButton(onClick = onDeleteClick) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Usuń użytkownika", tint = Color.Red)
                }
            }
        }
    }
}
