package com.example.apkazupy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apkazupy.data.Product

@Composable
fun ProductListScreen(viewModel: ProductViewModel, authViewModel: com.example.apkazupy.ui.AuthViewModel, onShowUsers: () -> Unit) {
    val products by viewModel.products.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val local by viewModel.localList.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Produkty", style = MaterialTheme.typography.h6)
            Row {
                currentUser?.let { Text("Zalogowany: ${it.login}", modifier = Modifier.padding(end = 8.dp)) }
                Button(onClick = { viewModel.loadProducts() }) { Text("Odśwież") }
                Spacer(modifier = Modifier.width(8.dp))
                // show users only for admin
                if (currentUser?.login == "admin") {
                    Button(onClick = onShowUsers) { Text("Użytkownicy") }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(onClick = { authViewModel.logout() }) { Text("Wyloguj") }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            Text("Ładowanie...")
        } else {
            if (products.isEmpty()) {
                Text("Brak produktów w bazie")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(products) { p ->
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(p.name ?: "<brak nazwy>", style = MaterialTheme.typography.subtitle1)
                                Text("Kod: ${p.barcode ?: "-"} • Kal: ${p.calories ?: "-"}")
                            }
                            Button(onClick = { viewModel.addToLocalList(p) }) { Text("Dodaj") }
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text("Lista lokalna", style = MaterialTheme.typography.h6)
        if (local.isEmpty()) {
            Text("Brak pozycji")
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                items(local) { p -> Text(p.name ?: "<brak nazwy>") }
            }
        }
    }
}

@Composable
fun ProductRow(p: Product, onAdd: (Product) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(p.name, style = MaterialTheme.typography.subtitle1)
            Text("Kal: ${p.calories ?: "-"} • Kod: ${p.barcode ?: "-"}")
        }
        Button(onClick = { onAdd(p) }) { Text("Dodaj") }
    }
}

@Composable
fun AddProductForm(onSubmit: (Product) -> Unit) {
    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa") })
        OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Kod") })
        OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Kalorie") })
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (name.isNotBlank()) {
                val product = Product(name = name, barcode = if (barcode.isBlank()) null else barcode,
                    calories = calories.toDoubleOrNull())
                onSubmit(product)
                name = ""; barcode = ""; calories = ""
            }
        }) { Text("Dodaj produkt (remote)") }
    }
}
