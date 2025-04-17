package org.example.frontend.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import org.example.frontend.api.ApiClient
import org.example.frontend.api.UserDTO
import org.example.frontend.components.layouts.MainLayout
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

val TableStyle = ComponentStyle("dashboard-table") {
    base {
        Modifier
            .fillMaxWidth()
            .margin(top = 1.rem)
            .border(1.px, LineStyle.Solid, Color.lightgray)
            .borderCollapse(BorderCollapse.Collapse)
    }
}

val TableHeaderStyle = ComponentStyle("table-header") {
    base {
        Modifier
            .backgroundColor(Color("#f2f2f2"))
            .padding(0.5.rem)
            .border(1.px, LineStyle.Solid, Color.lightgray)
            .fontWeight(FontWeight.Bold)
    }
}

val TableCellStyle = ComponentStyle("table-cell") {
    base {
        Modifier
            .padding(0.5.rem)
            .border(1.px, LineStyle.Solid, Color.lightgray)
    }
}

val ActionButtonStyle = ComponentStyle("action-button") {
    base {
        Modifier
            .padding(0.25.rem)
            .margin(right = 0.25.rem)
            .borderRadius(4.px)
            .border(0.px)
            .cursor(Cursor.Pointer)
    }
}

@Page
@Composable
fun DashboardPage() {
    val pageContext = rememberPageContext()
    var users by remember { mutableStateOf<List<UserDTO>>(emptyList()) }
    var currentUser by remember { mutableStateOf<UserDTO?>(null) }
    var isAdmin by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    
    // Check if user is logged in
    LaunchedEffect(Unit) {
        val token = localStorage.getItem("auth_token")
        if (token == null) {
            pageContext.router.navigateTo("/login")
            return@LaunchedEffect
        }
        
        // Get current user
        ApiClient.getCurrentUser()
            .onSuccess { user ->
                currentUser = user
                isAdmin = user.role == "admin"
                
                // If admin, load all users
                if (isAdmin) {
                    ApiClient.getUsers()
                        .onSuccess { userList ->
                            users = userList
                            isLoading = false
                        }
                        .onFailure {
                            error = it.message ?: "Failed to load users"
                            isLoading = false
                        }
                } else {
                    // If not admin, just show current user
                    users = listOf(user)
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
    
    MainLayout(title = "Dashboard") {
        Column(
            modifier = Modifier.fillMaxWidth().padding(2.rem),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().margin(bottom = 1.rem),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpanText(
                    text = "Welcome, ${currentUser?.username ?: "User"}!",
                    modifier = Modifier.fontSize(1.5.rem).margin(right = 1.rem)
                )
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            ApiClient.logout()
                                .onSuccess {
                                    pageContext.router.navigateTo("/login")
                                }
                        }
                    },
                    modifier = ButtonStyle.toModifier()
                ) {
                    Text("Logout")
                }
            }
            
            if (error.isNotEmpty()) {
                Box(ErrorStyle.toModifier()) {
                    Text(error)
                }
            }
            
            if (isLoading) {
                SpanText("Loading...")
            } else {
                if (isAdmin) {
                    SpanText(
                        text = "User Management",
                        modifier = Modifier.fontSize(1.2.rem).margin(bottom = 1.rem, top = 1.rem)
                    )
                }
                
                Table(TableStyle.toModifier()) {
                    Thead {
                        Tr {
                            Th(TableHeaderStyle.toModifier()) { Text("ID") }
                            Th(TableHeaderStyle.toModifier()) { Text("Username") }
                            Th(TableHeaderStyle.toModifier()) { Text("Email") }
                            Th(TableHeaderStyle.toModifier()) { Text("Role") }
                            Th(TableHeaderStyle.toModifier()) { Text("Actions") }
                        }
                    }
                    Tbody {
                        for (user in users) {
                            Tr {
                                Td(TableCellStyle.toModifier()) { Text(user.userId.toString()) }
                                Td(TableCellStyle.toModifier()) { Text(user.username) }
                                Td(TableCellStyle.toModifier()) { Text(user.email) }
                                Td(TableCellStyle.toModifier()) { Text(user.role) }
                                Td(TableCellStyle.toModifier()) {
                                    Button(
                                        onClick = {
                                            pageContext.router.navigateTo("/user/${user.userId}")
                                        },
                                        modifier = ActionButtonStyle.toModifier().backgroundColor(Color("#3f51b5")).color(Color.white)
                                    ) {
                                        Text("Edit")
                                    }
                                    
                                    if (isAdmin || currentUser?.userId == user.userId) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    if (confirm("Are you sure you want to delete this user?")) {
                                                        ApiClient.deleteUser(user.userId)
                                                            .onSuccess {
                                                                if (currentUser?.userId == user.userId) {
                                                                    // If deleting own account, logout
                                                                    ApiClient.logout()
                                                                        .onSuccess {
                                                                            pageContext.router.navigateTo("/login")
                                                                        }
                                                                } else {
                                                                    // Refresh user list
                                                                    ApiClient.getUsers()
                                                                        .onSuccess { userList ->
                                                                            users = userList
                                                                        }
                                                                }
                                                            }
                                                            .onFailure {
                                                                error = it.message ?: "Failed to delete user"
                                                            }
                                                    }
                                                }
                                            },
                                            modifier = ActionButtonStyle.toModifier().backgroundColor(Color.red).color(Color.white)
                                        ) {
                                            Text("Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun confirm(message: String): Boolean {
    return js("confirm(message)") as Boolean
}