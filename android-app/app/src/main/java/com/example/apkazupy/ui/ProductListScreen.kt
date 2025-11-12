// kotlin
package com.example.apkazupy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.apkazupy.ui.AppPrimary
import com.example.apkazupy.ui.AppOnPrimary
import androidx.compose.material.SliderDefaults
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import com.example.apkazupy.data.Product
import com.example.apkazupy.data.Suggestion
import com.example.apkazupy.network.createSuggestion
import androidx.compose.material.Card
import androidx.compose.material.Slider
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    authViewModel: com.example.apkazupy.ui.AuthViewModel,
    onShowUsers: () -> Unit,
    onShowSuggestions: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val currentUserState = authViewModel.currentUser.collectAsState()
    val currentUser = currentUserState.value
    val currentUserId = currentUser?.id

    var maxCalories by remember { mutableStateOf(1000f) }

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
        ) {
            TopAppBar(
                title = { Text("Apka Zakupy") },
                backgroundColor = AppPrimary,
                contentColor = AppOnPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.padding(12.dp)) {
                LaunchedEffect(currentUserId) {
                    if (currentUserId != null) {
                        viewModel.loadNamedListsRemote(currentUserId)
                        viewModel.loadListDetailsRemote(currentUserId)
                    } else {
                        viewModel.clearNamedLists()
                    }
                }

                if (currentUser?.login == "admin") {
                    Button(
                        onClick = onShowUsers,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)
                    ) {
                        Text("Zobacz użytkowników")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onShowSuggestions,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)
                    ) {
                        Text("Zobacz sugestie")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = { /* TODO: scan action */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)
                ) {
                    Text("Skanuj produkt spożywczy")
                }

                Spacer(modifier = Modifier.height(8.dp))

                var showSuggestDialog by remember { mutableStateOf(false) }
                if (showSuggestDialog) {
                    var sName by remember { mutableStateOf("") }
                    var sBarcode by remember { mutableStateOf("") }
                    var sCalories by remember { mutableStateOf("") }
                    var sComment by remember { mutableStateOf("") }
                    var sending by remember { mutableStateOf(false) }

                    AlertDialog(
                        onDismissRequest = { if (!sending) showSuggestDialog = false },
                        title = { Text("Dodaj sugestię produktu") },
                        text = {
                            Column {
                                OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("Nazwa produktu") })
                                OutlinedTextField(value = sBarcode, onValueChange = { sBarcode = it }, label = { Text("Kod") })
                                OutlinedTextField(value = sCalories, onValueChange = { sCalories = it.filter { c-> c.isDigit() || c=='.' } }, label = { Text("Kalorie") })
                                OutlinedTextField(value = sComment, onValueChange = { sComment = it }, label = { Text("Komentarz") })
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val cal = sCalories.toDoubleOrNull()
                                val suggestion = Suggestion(id = null, userId = currentUserId, productName = sName, barcode = if (sBarcode.isBlank()) null else sBarcode, calories = cal, comment = if (sComment.isBlank()) null else sComment)
                                sending = true
                                // wykonaj wywołanie sieciowe do backendu aby zapisać sugestię
                                coroutineScope.launch {
                                    try {
                                        val resp = createSuggestion(suggestion)
                                        if (resp.isSuccessful) {
                                            scaffoldState.snackbarHostState.showSnackbar("Sugestia wysłana")
                                            showSuggestDialog = false
                                        } else {
                                            scaffoldState.snackbarHostState.showSnackbar("Błąd serwera: ${resp.code()}")
                                        }
                                    } catch (e: Exception) {
                                        scaffoldState.snackbarHostState.showSnackbar("Błąd: ${e.message}")
                                    } finally {
                                        sending = false
                                    }
                                }
                            }) { Text("Wyślij", color = AppPrimary) }
                        },
                        dismissButton = { TextButton(onClick = { if (!sending) showSuggestDialog = false }) { Text("Anuluj") } }
                    )
                }

                Button(
                    onClick = { showSuggestDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)
                ) {
                    Text("Dodaj sugestię")
                }

                Spacer(modifier = Modifier.height(8.dp))

                var showAddListDialog by remember { mutableStateOf(false) }
                var addListError by remember { mutableStateOf<String?>(null) }
                var addListLoading by remember { mutableStateOf(false) }

                if (showAddListDialog) {
                    var listName by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = {
                            if (!addListLoading) {
                                showAddListDialog = false
                                addListError = null
                            }
                        },
                        title = { Text("Dodaj listę zakupów") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = listName,
                                    onValueChange = { listName = it },
                                    label = { Text("Nazwa listy") }
                                )
                                addListError?.let { Text(it, color = MaterialTheme.colors.error) }
                                if (addListLoading) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(color = AppPrimary)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (listName.isNotBlank()) {
                                        val uid = currentUserId
                                        if (uid != null) {
                                            addListLoading = true
                                            viewModel.addNamedListRemote(uid, listName) { ok, err ->
                                                addListLoading = false
                                                if (ok) {
                                                    showAddListDialog = false
                                                    addListError = null
                                                } else {
                                                    addListError = err ?: "Błąd sieci"
                                                }
                                            }
                                        } else {
                                            viewModel.addNamedList(listName)
                                            showAddListDialog = false
                                        }
                                    }
                                },
                                enabled = !addListLoading
                            ) {
                                Text("Dodaj", color = AppPrimary)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                if (!addListLoading) {
                                    showAddListDialog = false
                                    addListError = null
                                }
                            }) { Text("Anuluj") }
                        }
                    )
                }

                Button(
                    onClick = { showAddListDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)
                ) {
                    Text("Dodaj listę")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Produkty", style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Filtruj po maksymalnych kaloriach: ${maxCalories.toInt()}")
                        Slider(
                            value = maxCalories,
                            onValueChange = { maxCalories = it },
                            valueRange = 0f..2000f,
                            colors = SliderDefaults.colors(thumbColor = AppPrimary, activeTrackColor = AppPrimary)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        var searchQuery by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Szukaj produktów") },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Szukaj") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (loading) {
                            Text("Ładowanie...")
                        } else {
                            val filtered = products.filter { p ->
                                val byCalories = p.calories == null || (p.calories ?: 0.0) <= maxCalories.toDouble()
                                val byName = searchQuery.isBlank() || (p.name?.contains(searchQuery, ignoreCase = true) ?: false)
                                byCalories && byName
                            }
                            if (filtered.isEmpty()) {
                                Text("Brak produktów w bazie")
                            } else {
                                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                    items(filtered) { p ->
                                        ProductItemRow(p = p, viewModel = viewModel, currentUserId = currentUserId, onAdded = {
                                            coroutineScope.launch {
                                                scaffoldState.snackbarHostState.showSnackbar("Dodano do listy")
                                            }
                                        })
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        var sortAsc by remember { mutableStateOf(true) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Moje listy", style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))

                            IconButton(onClick = { sortAsc = !sortAsc }) {
                                Icon(
                                    imageVector = Icons.Filled.SortByAlpha,
                                    contentDescription = "Sortuj",
                                    tint = if (sortAsc) AppPrimary else Color.Unspecified
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        var listSearchQuery by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = listSearchQuery,
                            onValueChange = { listSearchQuery = it },
                            label = { Text("Szukaj list") },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Szukaj") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val details by viewModel.listDetails.collectAsState()
                        if (details.isEmpty()) {
                            Text("Brak list.")
                        } else {
                            Column {
                                val displayDetails = if (sortAsc) details.sortedBy { it.name?.lowercase() ?: "" }
                                else details.sortedByDescending { it.name?.lowercase() ?: "" }

                                val filteredDetails = if (listSearchQuery.isBlank()) displayDetails else displayDetails.filter { it.name?.contains(listSearchQuery, ignoreCase = true) ?: false }

                                filteredDetails.forEach { l ->
                                    var showListDialog by remember { mutableStateOf(false) }
                                    var showDeleteConfirm by remember { mutableStateOf(false) }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { showListDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = l.name ?: "<lista>",
                                                style = MaterialTheme.typography.subtitle1,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }

                                    if (showListDialog) {
                                        LaunchedEffect(l.id) { viewModel.loadListItemsRemote(l.id) }
                                        val items by viewModel.listItems.collectAsState()
                                        var showItemDetails by remember { mutableStateOf<Pair<Boolean, Long?>>(false to null) }
                                        val loadingMap: SnapshotStateMap<Long, Boolean> = remember { mutableStateMapOf() }
                                        var showAddProductPanel by remember { mutableStateOf(false) }
                                        val addLoadingMap: SnapshotStateMap<Long, Boolean> = remember { mutableStateMapOf() }

                                        AlertDialog(
                                            onDismissRequest = { showListDialog = false },
                                            title = { Text("Lista: ${l.name}") },
                                            text = {
                                                Column {
                                                    if (items.isEmpty()) {
                                                        Text("Brak produktów na liście")
                                                    } else {
                                                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                                            items(items) { it ->
                                                                val itemId = it.itemId ?: -1L
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(8.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Column(modifier = Modifier.weight(1f)) {
                                                                        Text(it.name ?: "<brak nazwy>")
                                                                        Spacer(modifier = Modifier.height(4.dp))
                                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                                            IconButton(onClick = {
                                                                                if (loadingMap[itemId] == true) return@IconButton
                                                                                loadingMap[itemId] = true
                                                                                val current = it.quantity ?: 1
                                                                                val newQ = current - 1
                                                                                if (newQ <= 0) {
                                                                                    viewModel.deleteItemRemote(l.id, itemId) { ok, _ ->
                                                                                        loadingMap[itemId] = false
                                                                                        if (ok) {
                                                                                            viewModel.loadListItemsRemote(l.id)
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    viewModel.updateItemQuantityRemote(l.id, itemId, newQ) { ok, _ ->
                                                                                        loadingMap[itemId] = false
                                                                                        if (ok) viewModel.loadListItemsRemote(l.id)
                                                                                    }
                                                                                }
                                                                            }) {
                                                                                Icon(imageVector = Icons.Filled.RemoveCircle, contentDescription = "Minus", tint = AppPrimary)
                                                                            }

                                                                            Text((it.quantity ?: 1).toString(), modifier = Modifier.padding(horizontal = 8.dp))

                                                                            IconButton(onClick = {
                                                                                if (loadingMap[itemId] == true) return@IconButton
                                                                                loadingMap[itemId] = true
                                                                                val current = it.quantity ?: 1
                                                                                val newQ = current + 1
                                                                                viewModel.updateItemQuantityRemote(l.id, itemId, newQ) { ok, _ ->
                                                                                    loadingMap[itemId] = false
                                                                                    if (ok) viewModel.loadListItemsRemote(l.id)
                                                                                }
                                                                            }) {
                                                                                Icon(imageVector = Icons.Filled.Add, contentDescription = "Plus", tint = AppPrimary)
                                                                            }
                                                                        }
                                                                    }

                                                                    Spacer(modifier = Modifier.width(8.dp))

                                                                    Column(horizontalAlignment = Alignment.End) {
                                                                        IconButton(onClick = { showItemDetails = true to itemId }) {
                                                                            Icon(imageVector = Icons.Filled.Info, contentDescription = "Szczegóły", tint = AppPrimary)
                                                                        }
                                                                        IconButton(onClick = {
                                                                            if (loadingMap[itemId] == true) return@IconButton
                                                                            loadingMap[itemId] = true
                                                                            viewModel.deleteItemRemote(l.id, itemId) { ok, _ ->
                                                                                loadingMap[itemId] = false
                                                                                if (ok) viewModel.loadListItemsRemote(l.id)
                                                                            }
                                                                        }) {
                                                                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Usuń", tint = Color.Red)
                                                                        }
                                                                    }

                                                                    if (showItemDetails.first && showItemDetails.second == itemId) {
                                                                        AlertDialog(
                                                                            onDismissRequest = { showItemDetails = false to null },
                                                                            title = { Text(it.name ?: "<brak nazwy>") },
                                                                            text = {
                                                                                Column {
                                                                                    Text("Kod: ${it.barcode ?: "-"}")
                                                                                    Text("Kalorie: ${it.calories ?: "-"}")
                                                                                    Text("Białko: ${it.protein ?: "-"}")
                                                                                    Text("Tłuszcz: ${it.fat ?: "-"}")
                                                                                    Text("Węglowodany: ${it.carbohydrates ?: "-"}")
                                                                                    Text("Ilość: ${it.quantity ?: 1}")
                                                                                }
                                                                            },
                                                                            confirmButton = {
                                                                                TextButton(onClick = { showItemDetails = false to null }) {
                                                                                    Text("Zamknij", color = AppPrimary)
                                                                                }
                                                                            }
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    val totalCalories = items.sumOf { (it.calories ?: 0.0) * (it.quantity ?: 1) }
                                                    val totalProtein = items.sumOf { (it.protein ?: 0.0) * (it.quantity ?: 1) }
                                                    val totalFat = items.sumOf { (it.fat ?: 0.0) * (it.quantity ?: 1) }
                                                    val totalCarbs = items.sumOf { (it.carbohydrates ?: 0.0) * (it.quantity ?: 1) }

                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Divider()
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Column {
                                                        Text("Suma kalorii: ${"%.1f".format(totalCalories)}")
                                                        Text("Białko: ${"%.1f".format(totalProtein)} g • Tłuszcz: ${"%.1f".format(totalFat)} g")
                                                        Text("Węglowodany: ${"%.1f".format(totalCarbs)} g")
                                                    }
                                                }
                                            },
                                            confirmButton = {},
                                            dismissButton = {
                                                Row {
                                                    TextButton(onClick = { showAddProductPanel = true }) {
                                                        Text("Dodaj produkt", color = AppPrimary)
                                                    }
                                                    TextButton(onClick = { showDeleteConfirm = true }) {
                                                        Text("Usuń", color = Color.Red)
                                                    }
                                                    TextButton(onClick = { showListDialog = false }) {
                                                        Text("Zamknij", color = AppPrimary)
                                                    }
                                                }
                                            }
                                        )

                                        if (showAddProductPanel) {
                                            AlertDialog(
                                                onDismissRequest = { showAddProductPanel = false },
                                                title = { Text("Dodaj produkt do: ${l.name}") },
                                                text = {
                                                    Column {
                                                        if (products.isEmpty()) {
                                                            Text("Brak produktów w bazie")
                                                        } else {
                                                            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                                                items(products) { p ->
                                                                    val pid = p.id ?: -1L
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .padding(8.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Column(modifier = Modifier.weight(1f)) {
                                                                            Text(p.name ?: "<brak nazwy>")
                                                                            Text("Kal: ${p.calories ?: "-"}")
                                                                        }
                                                                        IconButton(onClick = {
                                                                            if (addLoadingMap[pid] == true) return@IconButton
                                                                            addLoadingMap[pid] = true
                                                                            val productId = p.id ?: -1L
                                                                            viewModel.addProductToListRemote(l.id, productId, 1) { ok, _ ->
                                                                                addLoadingMap[pid] = false
                                                                                if (ok) {
                                                                                    viewModel.loadListItemsRemote(l.id)
                                                                                    coroutineScope.launch {
                                                                                        scaffoldState.snackbarHostState.showSnackbar("Dodano do listy")
                                                                                    }
                                                                                }
                                                                            }
                                                                        }) {
                                                                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Dodaj", tint = AppPrimary)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                confirmButton = {},
                                                dismissButton = {
                                                    TextButton(onClick = { showAddProductPanel = false }) {
                                                        Text("Zamknij", color = AppPrimary)
                                                    }
                                                }
                                            )
                                        }
                                    } // koniec showListDialog
                                    if (showDeleteConfirm) {
                                        AlertDialog(
                                            onDismissRequest = { showDeleteConfirm = false },
                                            title = { Text("Usuń listę") },
                                            text = { Text("Czy na pewno chcesz usunąć listę '${l.name}'? To usunie wszystkie przedmioty na tej liście.") },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    viewModel.deleteListRemote(l.id, currentUserId) { ok, _ ->
                                                        showDeleteConfirm = false
                                                        if (ok) {
                                                            // zamknij dialog szczegółów jeżeli otwarty
                                                            // showListDialog = false // nie musimy modyfikować tutaj stanu zewnętrznego
                                                        }
                                                    }
                                                }) { Text("Usuń", color = Color.Red) }
                                            },
                                            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Anuluj") } }
                                        )
                                    }
                                } // koniec foreach displayDetails
                            } // koniec Column dla list
                        } // koniec else details.isEmpty
                    } // koniec Column w Card
                } // koniec Card
            } // koniec Column głównej
        } // koniec Column root
    } // koniec Scaffold
} // koniec ProductListScreen

@Composable
private fun ProductItemRow(p: Product, viewModel: ProductViewModel, currentUserId: Long?, onAdded: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(p.name ?: "<brak nazwy>", style = MaterialTheme.typography.subtitle1)
            Text("Kal: ${p.calories ?: "-"}")
        }

        var showDetails by remember { mutableStateOf(false) }
        IconButton(onClick = { showDetails = true }) {
            Icon(imageVector = Icons.Filled.Info, contentDescription = "Szczegóły", tint = AppPrimary)
        }

        val addButtonSize: Dp = 44.dp
        var showAddToListDialog by remember { mutableStateOf(false) }

        if (showAddToListDialog) {
            var selectedListId by remember { mutableStateOf<Long?>(null) }
            var qtyText by remember { mutableStateOf("1") }
            var showCreateInline by remember { mutableStateOf(false) }
            var newListName by remember { mutableStateOf("") }
            var creating by remember { mutableStateOf(false) }
            var listSelectionError by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(currentUserId) {
                if (currentUserId != null) {
                    viewModel.loadListDetailsRemote(currentUserId)
                }
            }

            val lists by viewModel.listDetails.collectAsState()

            AlertDialog(
                onDismissRequest = { showAddToListDialog = false },
                title = { Text("Dodaj do listy") },
                text = {
                    Column {
                        if (currentUserId == null) {
                            Text("Jesteś niezalogowany. Produkt zostanie dodany do lokalnej listy.")
                        }
                        if (lists.isEmpty()) {
                            Text("Brak istniejących list.")
                        } else {
                            lists.forEach { l ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectedListId == l.id, onClick = { selectedListId = l.id; listSelectionError = null })
                                    Text(text = l.name ?: "<lista>")
                                }
                            }
                            listSelectionError?.let { Text(it, color = MaterialTheme.colors.error) }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!showCreateInline) {
                            TextButton(onClick = { showCreateInline = true }) { Text("Utwórz nową listę...") }
                        } else {
                            Column {
                                OutlinedTextField(
                                    value = newListName,
                                    onValueChange = { newListName = it },
                                    label = { Text("Nazwa nowej listy") }
                                )
                                Row {
                                    TextButton(onClick = {
                                        if (newListName.isBlank()) return@TextButton
                                        creating = true
                                        viewModel.addNamedListRemote(currentUserId, newListName) { ok, _ ->
                                            creating = false
                                            if (ok) {
                                                viewModel.loadListDetailsRemote(currentUserId)
                                            } else {
                                                showAddToListDialog = false
                                            }
                                        }
                                    }, enabled = !creating) {
                                        Text("Utwórz i dodaj", color = AppPrimary)
                                    }
                                    TextButton(onClick = { showCreateInline = false }) { Text("Anuluj") }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it.filter { c -> c.isDigit() } },
                            label = { Text("Ilość") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val qty = qtyText.toIntOrNull() ?: 1
                        if (currentUserId != null && selectedListId == null && !showCreateInline) {
                            listSelectionError = "Wybierz listę lub utwórz nową"
                            return@TextButton
                        }
                        if (currentUserId == null) {
                            repeat(qty) { viewModel.addToLocalList(p) }
                            showAddToListDialog = false
                        } else {
                            if (selectedListId != null) {
                                viewModel.addProductToListRemote(selectedListId!!, p.id ?: -1L, qty) { ok, _ ->
                                    if (ok) {
                                        showAddToListDialog = false
                                        onAdded()
                                    }
                                }
                            } else {
                                val newListNameAuto = p.name ?: "Lista"
                                viewModel.addNamedListRemote(currentUserId, newListNameAuto) { ok, _ ->
                                    if (ok) {
                                        viewModel.loadListDetailsRemote(currentUserId)
                                        val created = viewModel.listDetails.value.firstOrNull { it.name == newListNameAuto }
                                        if (created != null) {
                                            viewModel.addProductToListRemote(created.id, p.id ?: -1L, qty) { ok2, _ ->
                                                if (ok2) {
                                                    showAddToListDialog = false
                                                    onAdded()
                                                }
                                            }
                                        } else {
                                            showAddToListDialog = false
                                        }
                                    } else {
                                        showAddToListDialog = false
                                    }
                                }
                            }
                        }
                    }) { Text("OK", color = AppPrimary) }
                },
                dismissButton = {
                    TextButton(onClick = { showAddToListDialog = false }) { Text("Anuluj") }
                }
            )
        }

        Button(
            onClick = { showAddToListDialog = true },
            modifier = Modifier.size(addButtonSize),
            colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)
        ) {
            Text("+", style = MaterialTheme.typography.h6)
        }

        if (showDetails) {
            AlertDialog(
                onDismissRequest = { showDetails = false },
                title = { Text(text = p.name ?: "<brak nazwy>") },
                text = {
                    Column {
                        Text("Kod: ${p.barcode ?: "-"}")
                        Text("Kalorie: ${p.calories ?: "-"}")
                        Text("Białko: ${p.protein ?: "-"}")
                        Text("Tłuszcz: ${p.fat ?: "-"}")
                        Text("Węglowodany: ${p.carbohydrates ?: "-"}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDetails = false }) { Text("Zamknij", color = AppPrimary) }
                }
            )
        }
    }
}

@Composable
fun ProductRow(p: Product, onAdd: (Product) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Kal: ${p.calories ?: "-"} • Kod: ${p.barcode ?: "-"}")
        }
        Button(onClick = { onAdd(p) }, colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)) {
            Text("Dodaj")
        }
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
                val product = Product(
                    name = name,
                    barcode = if (barcode.isBlank()) null else barcode,
                    calories = calories.toDoubleOrNull()
                )
                onSubmit(product)
                name = ""; barcode = ""; calories = ""
            }
        }, colors = ButtonDefaults.buttonColors(backgroundColor = AppPrimary, contentColor = AppOnPrimary)) {
            Text("Dodaj produkt (remote)")
        }
    }
}
