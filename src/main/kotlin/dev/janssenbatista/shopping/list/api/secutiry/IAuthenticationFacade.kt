package dev.janssenbatista.shopping.list.api.secutiry

import org.springframework.security.core.Authentication

interface IAuthenticationFacade {
    fun getAuthentication(): Authentication
}