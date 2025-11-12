// kotlin
package com.example.apkazupy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import com.example.apkazupy.ui.ProductListScreen
import com.example.apkazupy.ui.ProductViewModel
import com.example.apkazupy.ui.AuthViewModel
import com.example.apkazupy.ui.AuthScreen
import com.example.apkazupy.ui.UserListScreen
import com.example.apkazupy.ui.SuggestionsListScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val viewModel by lazy { ProductViewModel() }
    private val authViewModel by lazy { AuthViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    var screen by remember { mutableStateOf("auth") }

                    val currentUser by authViewModel.currentUser.collectAsState()
                    LaunchedEffect(currentUser) {
                        screen = if (currentUser != null) "main" else "auth"
                    }

                    when (screen) {
                        "auth" -> AuthScreen(authViewModel) { screen = "main" }
                        "main" -> {
                            // Usuń try/catch wokół wywołań kompozycji.
                            ProductListScreen(viewModel, authViewModel, { screen = "users" }, { screen = "suggestions" })
                        }
                        "users" -> UserListScreen(authViewModel) { screen = "main" }
                        "suggestions" -> SuggestionsListScreen(authViewModel) { screen = "main" }
                        else -> AuthScreen(authViewModel) { screen = "main" }
                    }
                }
            }
        }
    }
}
