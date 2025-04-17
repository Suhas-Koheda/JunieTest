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
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import org.example.frontend.api.ApiClient
import org.example.frontend.api.UserDTO
import org.example.frontend.components.layouts.MainLayout
import org.jetbrains.compose.web.dom.Text

@Page("user/{userId}")
@Composable
fun UserEditPage() {
    val pageContext = rememberPageContext()
    val userId = pageContext.route.params["userId"]?.toIntOrNull()
    var user by remember { mutableStateOf<UserDTO?>(null) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    
    // Check if user is logged in
    LaunchedEffect(Unit) {
        val token = localStorage.getItem("auth_token")
        if (token == null) {
            pageContext.router.navigateTo("/login")
            return@LaunchedEffect
        }
        
        // Get current user to check permissions
        ApiClient.getCurrentUser()
            .onSuccess { currentUser ->
                if (userId == null) {
                    error = "Invalid user ID"
                    isLoading = false
                    return@onSuccess
                }
                
                // Check if user has permission to edit this profile
                val isAdmin = currentUser.role == "admin"
                if (userId != currentUser.userId && !isAdmin) {
                    error = "You don't have permission to edit this user"
                    isLoading = false
                    return@onSuccess
                }
                
                // Get user details
                ApiClient.getUser(userId)
                    .onSuccess { userDetails ->
                        user = userDetails
                        username = userDetails.username
                        email = userDetails.email
                        isLoading = false
                    }
                    .onFailure {
                        error = it.message ?: "Failed to load user details"
                        isLoading = false
                    }
            }
            .onFailure {
                error = it.message ?: "Failed to get user info"
                isLoading = false
                // Redirect to login if authentication fails
                pageContext.router.navigateTo("/login")
            }
    }
    
    MainLayout(title = "Edit User") {
        Column(
            modifier = FormStyle.toModifier(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpanText(
                text = "Edit User Profile",
                modifier = Modifier.fontSize(1.5.rem).margin(bottom = 1.rem)
            )
            
            if (error.isNotEmpty()) {
                Box(ErrorStyle.toModifier()) {
                    Text(error)
                }
            }
            
            if (success.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.5.rem)
                        .color(Color.green)
                        .textAlign(TextAlign.Center)
                ) {
                    Text(success)
                }
            }
            
            if (isLoading) {
                SpanText("Loading...")
            } else if (user != null) {
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
                
                Button(
                    onClick = {
                        if (username.isBlank() || email.isBlank()) {
                            error = "Please fill in all fields"
                            return@Button
                        }
                        
                        if (!email.contains("@")) {
                            error = "Please enter a valid email address"
                            return@Button
                        }
                        
                        coroutineScope.launch {
                            error = ""
                            success = ""
                            isLoading = true
                            
                            ApiClient.updateUser(
                                userId = userId ?: 0,
                                username = username,
                                email = email
                            )
                                .onSuccess {
                                    success = "User updated successfully"
                                    isLoading = false
                                }
                                .onFailure {
                                    error = it.message ?: "Failed to update user"
                                    isLoading = false
                                }
                        }
                    },
                    modifier = ButtonStyle.toModifier(),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Updating..." else "Update")
                }
                
                Button(
                    onClick = {
                        pageContext.router.navigateTo("/dashboard")
                    },
                    modifier = ButtonStyle.toModifier().margin(top = 1.rem).backgroundColor(Color.gray),
                    enabled = !isLoading
                ) {
                    Text("Back to Dashboard")
                }
            }
        }
    }
}