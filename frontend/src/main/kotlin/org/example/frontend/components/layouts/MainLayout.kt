package org.example.frontend.components.layouts

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text

val MainLayoutStyle = ComponentStyle("main-layout") {
    base {
        Modifier
            .fillMaxWidth()
            .minHeight(100.vh)
            .backgroundColor(Color.white)
            .fontFamily("Roboto, sans-serif")
    }
    
    ColorMode.DARK.invoke {
        Modifier.backgroundColor(Color("#121212"))
    }
}

val HeaderStyle = ComponentStyle("header") {
    base {
        Modifier
            .fillMaxWidth()
            .padding(1.rem)
            .backgroundColor(Color("#3f51b5"))
            .color(Color.white)
            .textAlign(TextAlign.Center)
    }
    
    ColorMode.DARK.invoke {
        Modifier.backgroundColor(Color("#1a237e"))
    }
}

val ContentStyle = ComponentStyle("content") {
    base {
        Modifier
            .fillMaxWidth()
            .padding(2.rem)
    }
}

@Composable
fun MainLayout(title: String = "Auth System", content: @Composable () -> Unit) {
    Box(MainLayoutStyle.toModifier()) {
        Column(Modifier.fillMaxWidth()) {
            // Header
            Box(HeaderStyle.toModifier()) {
                H1 {
                    Text(title)
                }
            }
            
            // Content
            Box(
                ContentStyle.toModifier(),
                contentAlignment = Alignment.TopCenter
            ) {
                content()
            }
        }
    }
}