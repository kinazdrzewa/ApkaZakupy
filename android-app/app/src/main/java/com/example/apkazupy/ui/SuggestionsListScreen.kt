
package com.example.apkazupy.ui

import com.example.apkazupy.network.createProduct
import com.example.apkazupy.ui.ProductViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apkazupy.data.Suggestion
import com.example.apkazupy.network.getSuggestions
import com.example.apkazupy.network.deleteSuggestion
import com.example.apkazupy.network.createSuggestion
import com.example.apkazupy.data.Product
import com.example.apkazupy.ui.AppPrimary
import com.example.apkazupy.ui.AppOnPrimary
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun SuggestionsListScreen(productViewModel: ProductViewModel, authViewModel: AuthViewModel, onBack: () -> Unit) {
    val current = authViewModel.currentUser.collectAsState().value
    var suggestions by remember { mutableStateOf<List<Suggestion>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    var sortDescending by remember { mutableStateOf(true) }

    LaunchedEffect(current) {
        loading = true
        try {
            suggestions = getSuggestions()
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    // Helper: try to convert various possible `createdAt` representations into epoch millis
    fun parseCreatedAtToEpoch(createdAt: Any?): Long {
        if (createdAt == null) return 0L
        try {
            when (createdAt) {
                is String -> {
                    // try ISO_LOCAL_DATE_TIME first
                    return try {
                        val dt = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        dt.toInstant(ZoneOffset.UTC).toEpochMilli()
                    } catch (_: Exception) {
                        // fallthrough to other parsing attempts
                        0L
                    }
                }
                is Number -> {
                    val v = createdAt.toLong()
                    return if (v > 1_000_000_000_000L) v else v * 1000L
                }
                is Map<*, *> -> {
                    val year = (createdAt["year"] as? Number)?.toInt() ?: (createdAt["getYear"] as? Number)?.toInt()
                    val month = (createdAt["monthValue"] as? Number)?.toInt() ?: (createdAt["month"] as? Number)?.toInt()
                    val day = (createdAt["dayOfMonth"] as? Number)?.toInt() ?: (createdAt["day"] as? Number)?.toInt()
                    val hour = (createdAt["hour"] as? Number)?.toInt() ?: 0
                    val minute = (createdAt["minute"] as? Number)?.toInt() ?: 0
                    val second = (createdAt["second"] as? Number)?.toInt() ?: 0
                    if (year != null && month != null && day != null) {
                        val dt = LocalDateTime.of(year, month, day, hour, minute, second)
                        return dt.toInstant(ZoneOffset.UTC).toEpochMilli()
                    }
                }
            }
        } catch (_: Exception) {
        }
        return 0L
    }

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sugestie produktów", style = MaterialTheme.typography.h6, color = AppPrimary)
                Column(horizontalAlignment = Alignment.End) {
                    Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)) { Text("Powrót") }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { sortDescending = !sortDescending }) {
                            Icon(
                                imageVector = if (sortDescending) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                                contentDescription = "Sortuj po dacie"
                            )
                        }
                        Text("Dodano", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            if (loading) {
                Text("Ładowanie...")
            } else if (error != null) {
                Text(error ?: "Nieznany błąd", color = MaterialTheme.colors.error)
            } else {
                if (suggestions.isEmpty()) {
                    Text("Brak sugestii")
                } else {
                    val sorted = if (sortDescending) suggestions.sortedByDescending { parseCreatedAtToEpoch(it.createdAt) } else suggestions.sortedBy { parseCreatedAtToEpoch(it.createdAt) }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(sorted) { s ->
                            var showDetails by remember { mutableStateOf(false) }
                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable(enabled = (current != null && current.login == "admin")) { if (current != null && current.login == "admin") showDetails = true }
                            ) {
                                Text(s.productName ?: "<brak>", style = MaterialTheme.typography.subtitle1)
                                Text("Użytkownik: ${s.userId ?: "-"}")
                                Text("Kalorie: ${s.calories ?: "-"}  Białko: ${s.protein ?: "-"}  Tłuszcz: ${s.fat ?: "-"}  Węglowodany: ${s.carbohydrates ?: "-"}")
                                if (!s.comment.isNullOrBlank()) Text("Komentarz: ${s.comment}")
                                s.createdAt?.let { Text("Dodano: ${it}") }
                                Divider(modifier = Modifier.padding(top = 8.dp))

                                if (showDetails) {
                                    var nameText by remember { mutableStateOf(s.productName ?: "") }
                                    var barcodeText by remember { mutableStateOf(s.barcode ?: "") }
                                    var caloriesText by remember { mutableStateOf(s.calories?.toString() ?: "") }
                                    var proteinText by remember { mutableStateOf(s.protein?.toString() ?: "") }
                                    var fatText by remember { mutableStateOf(s.fat?.toString() ?: "") }
                                    var carbsText by remember { mutableStateOf(s.carbohydrates?.toString() ?: "") }
                                    var commentText by remember { mutableStateOf(s.comment ?: "") }

                                    AlertDialog(
                                        onDismissRequest = { showDetails = false },
                                        title = { Text("Szczegóły sugestii") },
                                        text = {
                                            Column {
                                                OutlinedTextField(value = nameText, onValueChange = { nameText = it }, label = { Text("Nazwa") })
                                                OutlinedTextField(value = barcodeText, onValueChange = { barcodeText = it }, label = { Text("Kod") })
                                                OutlinedTextField(value = caloriesText, onValueChange = { caloriesText = it.filter { c -> c.isDigit() || c=='.' } }, label = { Text("Kalorie") })
                                                OutlinedTextField(value = proteinText, onValueChange = { proteinText = it.filter { c -> c.isDigit() || c=='.' } }, label = { Text("Białko (g)") })
                                                OutlinedTextField(value = fatText, onValueChange = { fatText = it.filter { c -> c.isDigit() || c=='.' } }, label = { Text("Tłuszcz (g)") })
                                                OutlinedTextField(value = carbsText, onValueChange = { carbsText = it.filter { c -> c.isDigit() || c=='.' } }, label = { Text("Węglowodany (g)") })
                                                OutlinedTextField(value = commentText, onValueChange = { commentText = it }, label = { Text("Komentarz") })
                                            }
                                        },
                                        confirmButton = {
                                            Row {
                                                TextButton(onClick = {
                                                    // Save edited suggestion by creating a new suggestion and deleting the old one
                                                    scope.launch {
                                                        try {
                                                            val newSuggestion = Suggestion(
                                                                id = null,
                                                                userId = s.userId,
                                                                productName = if (nameText.isBlank()) null else nameText,
                                                                barcode = if (barcodeText.isBlank()) null else barcodeText,
                                                                calories = caloriesText.toDoubleOrNull(),
                                                                protein = proteinText.toDoubleOrNull(),
                                                                fat = fatText.toDoubleOrNull(),
                                                                carbohydrates = carbsText.toDoubleOrNull(),
                                                                comment = if (commentText.isBlank()) null else commentText,
                                                                createdAt = s.createdAt
                                                            )
                                                            val resp = createSuggestion(newSuggestion)
                                                            if (resp.isSuccessful) {
                                                                if (s.id != null) {
                                                                    try { deleteSuggestion(s.id) } catch (_: Exception) { }
                                                                }
                                                                suggestions = getSuggestions()
                                                                scaffoldState.snackbarHostState.showSnackbar("Sugestia zaktualizowana")
                                                            } else {
                                                                scaffoldState.snackbarHostState.showSnackbar("Błąd serwera: ${resp.code()}")
                                                            }
                                                        } catch (e: Exception) {
                                                            scaffoldState.snackbarHostState.showSnackbar("Błąd: ${e.message}")
                                                        }
                                                        showDetails = false
                                                    }
                                                }) { Text("Zapisz") }

                                                TextButton(onClick = {
                                                    // Add as product and remove suggestion
                                                    scope.launch {
                                                        try {
                                                            val prod = Product(
                                                                id = null,
                                                                name = nameText.ifBlank { "" },
                                                                barcode = if (barcodeText.isBlank()) null else barcodeText,
                                                                calories = caloriesText.toDoubleOrNull(),
                                                                protein = proteinText.toDoubleOrNull(),
                                                                fat = fatText.toDoubleOrNull(),
                                                                carbohydrates = carbsText.toDoubleOrNull()
                                                            )
                                                            val created = createProduct(prod)
                                                            // best-effort: remove the suggestion after creating product
                                                            if (s.id != null) {
                                                                try {
                                                                    deleteSuggestion(s.id)
                                                                } catch (_: Exception) {
                                                                    // ignore deletion failure; UI will refresh suggestions list
                                                                }
                                                            }
                                                            scaffoldState.snackbarHostState.showSnackbar("Produkt dodany: ${created.id}")
                                                            // refresh product list so UI shows newly added product
                                                            productViewModel.loadProducts()
                                                            suggestions = getSuggestions()
                                                        } catch (e: Exception) {
                                                            scaffoldState.snackbarHostState.showSnackbar("Błąd podczas dodawania produktu: ${e.message}")
                                                        }
                                                        showDetails = false
                                                    }
                                                }) { Text("Dodaj produkt") }

                                                TextButton(onClick = { showDetails = false }) { Text("Zamknij") }
                                            }
                                        },
                                        dismissButton = { TextButton(onClick = { showDetails = false }) { Text("Anuluj") } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
