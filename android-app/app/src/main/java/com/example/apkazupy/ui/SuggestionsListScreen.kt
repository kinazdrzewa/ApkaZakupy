package com.example.apkazupy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apkazupy.data.Suggestion
import com.example.apkazupy.network.NetworkModule
import com.example.apkazupy.ui.AppPrimary
import com.example.apkazupy.ui.AppOnPrimary
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@Composable
fun SuggestionsListScreen(authViewModel: AuthViewModel, onBack: () -> Unit) {
    val current = authViewModel.currentUser.collectAsState().value
    var suggestions by remember { mutableStateOf<List<Suggestion>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(current) {
        if (current != null && current.login == "admin") {
            loading = true
            try {
                suggestions = NetworkModule.suggestionApi.getAll()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        } else {
            error = "Dostęp zabroniony: wymagany admin"
        }
    }

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(innerPadding).padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sugestie produktów", style = MaterialTheme.typography.h6, color = AppPrimary)
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)) { Text("Powrót") }
            }

            if (loading) Text("Ładowanie...")
            error?.let { Text(it, color = MaterialTheme.colors.error) }

            if (!loading && error == null) {
                if (suggestions.isEmpty()) Text("Brak sugestii") else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(suggestions) { s ->
                            var showDeleteConfirm by remember { mutableStateOf(false) }
                            var showDetails by remember { mutableStateOf(false) }
                            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                Text(s.productName ?: "<brak>", style = MaterialTheme.typography.subtitle1)
                                Text(s.comment ?: "")
                                Text("id: ${s.id ?: "-"}")
                                Row {
                                    TextButton(onClick = { showDetails = true }) { Text("Szczegóły") }
                                    IconButton(onClick = { showDeleteConfirm = true }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Usuń", tint = Color.Red)
                                    }
                                }
                            }

                            if (showDetails) {
                                AlertDialog(
                                    onDismissRequest = { showDetails = false },
                                    title = { Text(s.productName ?: "Szczegóły sugestii") },
                                    text = {
                                        Column {
                                            Text("ID: ${s.id ?: "-"}")
                                            Text("Użytkownik: ${s.userId ?: "-"}")
                                            Text("Kod: ${s.barcode ?: "-"}")
                                            Text("Kalorie: ${s.calories ?: "-"}")
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Komentarz:")
                                            Text(s.comment ?: "-")
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Utworzono: ${s.createdAt ?: "-"}")
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { showDetails = false }) { Text("Zamknij", color = AppPrimary) }
                                    },
                                    dismissButton = { TextButton(onClick = { showDetails = false }) { Text("Anuluj") } }
                                )
                            }

                            if (showDeleteConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirm = false },
                                    title = { Text("Usuń sugestię") },
                                    text = { Text("Czy na pewno chcesz usunąć sugestię id=${s.id}?") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            scope.launch {
                                                try {
                                                    val resp = NetworkModule.suggestionApi.delete(s.id ?: -1L)
                                                    if (resp.isSuccessful) {
                                                        suggestions = NetworkModule.suggestionApi.getAll()
                                                        scaffoldState.snackbarHostState.showSnackbar("Sugestia usunięta")
                                                    } else {
                                                        scaffoldState.snackbarHostState.showSnackbar("Błąd: ${resp.code()}")
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
