package dev.janssenbatista.shopping.list.api.models.dtos

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserDTO(
        @field:NotBlank(message = "Email cannot be blank")
        @field:Email(message = "Invalid email")
        var email: String,
        @field:NotBlank(message = "Password cannot be blank")
        @field:Size(min = 8, max = 64, message = "Password must contain between 8 and 64 characters")
        val password: String
)
