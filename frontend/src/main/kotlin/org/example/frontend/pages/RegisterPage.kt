package org.example.frontend.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.coroutines.launch
import org.example.frontend.api.ApiClient
import org.example.frontend.components.layouts.MainLayout
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Page
@Composable
fun RegisterPage() {
    val pageContext = rememberPageContext()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    MainLayout(title = "Register") {
        Column(
            modifier = FormStyle.toModifier(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpanText(
                text = "Register",
                modifier = Modifier.fontSize(1.5.rem).margin(bottom = 1.rem)
            )
            
            if (error.isNotEmpty()) {
                Box(ErrorStyle.toModifier()) {
                    Text(error)
                }
            }
            
            TextInput(
                value = username,
                onValueChanged = { username = it },
                modifier = InputStyle.toModifier(),
                placeholder = "Username"
            )
            
            TextInput(
                value = email,
                onValueChanged = { email = it },
                modifier = InputStyle.toModifier(),
                placeholder = "Email"
            )
            
            TextInput(
                value = password,
                onValueChanged = { password = it },
                modifier = InputStyle.toModifier(),
                placeholder = "Password",
                type = "password"
            )
            
            TextInput(
                value = confirmPassword,
                onValueChanged = { confirmPassword = it },
                modifier = InputStyle.toModifier(),
                placeholder = "Confirm Password",
                type = "password"
            )
            
            Button(
                onClick = {
                    if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        error = "Please fill in all fields"
                        return@Button
                    }
                    
                    if (password != confirmPassword) {
                        error = "Passwords do not match"
                        return@Button
                    }
                    
                    if (password.length < 8) {
                        error = "Password must be at least 8 characters long"
                        return@Button
                    }
                    
                    if (!email.contains("@")) {
                        error = "Please enter a valid email address"
                        return@Button
                    }
                    
                    coroutineScope.launch {
                        isLoading = true
                        error = ""
                        
                        ApiClient.register(username, email, password)
                            .onSuccess {
                                // Navigate to login page after successful registration
                                pageContext.router.navigateTo("/login")
                            }
                            .onFailure {
                                error = it.message ?: "Registration failed"
                            }
                        
                        isLoading = false
                    }
                },
                modifier = ButtonStyle.toModifier(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Registering..." else "Register")
            }
            
            P {
                Text("Already have an account? ")
                com.varabyte.kobweb.silk.components.navigation.Link(
                    path = "/login",
                    text = "Login"
                )
            }
        }
    }
}