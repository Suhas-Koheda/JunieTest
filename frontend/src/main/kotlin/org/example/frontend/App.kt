package org.example.frontend

import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.core.init.InitKobweb
import com.varabyte.kobweb.core.init.InitKobwebContext
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import kotlinx.browser.localStorage

@App
class AuthSystemApp : App() {
    override fun initKobweb(ctx: InitKobwebContext) {
        ctx.router.register("/") {
            ctx.router.navigateTo("/login")
        }
    }

    override fun initSilk(ctx: InitSilkContext) {
        // Initialize Silk with the default theme
        InitSilk(ctx)

        // Set the initial color mode based on user preference or system setting
        val savedColorMode = localStorage.getItem("colorMode")
        if (savedColorMode != null) {
            ctx.theme.colorMode = ColorMode.valueOf(savedColorMode)
        }
    }
}