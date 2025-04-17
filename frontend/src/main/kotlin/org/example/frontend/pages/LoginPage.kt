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
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.coroutines.launch
import org.example.frontend.api.ApiClient
import org.example.frontend.components.layouts.MainLayout
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

val FormStyle = ComponentStyle("login-form") {
    base {
        Modifier
            .maxWidth(400.px)
            .padding(2.rem)
            .border(1.px, LineStyle.Solid, Color.lightgray)
            .borderRadius(8.px)
            .backgroundColor(Color.white)
            .boxShadow(0.px, 2.px, 4.px, Color.lightgray)
    }
}

val InputStyle = ComponentStyle("login-input") {
    base {
        Modifier
            .fillMaxWidth()
            .margin(bottom = 1.rem)
            .padding(0.5.rem)
            .border(1.px, LineStyle.Solid, Color.lightgray)
            .borderRadius(4.px)
    }
}

val ButtonStyle = ComponentStyle("login-button") {
    base {
        Modifier
            .fillMaxWidth()
            .padding(0.5.rem)
            .backgroundColor(Color("#3f51b5"))
            .color(Color.white)
            .borderRadius(4.px)
            .border(0.px)
            .cursor(Cursor.Pointer)
    }
}

val ErrorStyle = ComponentStyle("login-error") {
    base {
        Modifier
            .fillMaxWidth()
            .padding(0.5.rem)
            .color(Color.red)
            .textAlign(TextAlign.Center)
    }
}

@Page
@Composable
fun LoginPage() {
    val pageContext = rememberPageContext()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    MainLayout(title = "Login") {
        Column(
            modifier = FormStyle.toModifier(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpanText(
                text = "Login",
                modifier = Modifier.fontSize(1.5.rem).margin(bottom = 1.rem)
            )
            
            if (error.isNotEmpty()) {
                Box(ErrorStyle.toModifier()) {
                    Text(error)
                }
            }
            
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
            
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        error = "Please fill in all fields"
                        return@Button
                    }
                    
                    coroutineScope.launch {
                        isLoading = true
                        error = ""
                        
                        ApiClient.login(email, password)
                            .onSuccess {
                                pageContext.router.navigateTo("/dashboard")
                            }
                            .onFailure {
                                error = it.message ?: "Login failed"
                            }
                        
                        isLoading = false
                    }
                },
                modifier = ButtonStyle.toModifier(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Logging in..." else "Login")
            }
            
            P {
                Text("Don't have an account? ")
                com.varabyte.kobweb.silk.components.navigation.Link(
                    path = "/register",
                    text = "Register"
                )
            }
        }
    }
}