package dev.janssenbatista.shopping.list.api.controllers

import dev.janssenbatista.shopping.list.api.models.User
import dev.janssenbatista.shopping.list.api.models.dtos.UserDTO
import dev.janssenbatista.shopping.list.api.models.dtos.UserResponseDTO
import dev.janssenbatista.shopping.list.api.services.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun createUser(@RequestBody @Valid userDTO: UserDTO): ResponseEntity<Any> {
        val createdUser = userService.save(User(email = userDTO.email, userPassword = userDTO.password))
        val userResponseDTO = UserResponseDTO(createdUser.id, createdUser.email)
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO)
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable(value = "id") userId: UUID, authentication: Authentication): ResponseEntity<User> {
        if (getUser(authentication).id != userId)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(userService.findUserById(userId))
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable(value = "id") userId: UUID,
                   @RequestBody @Valid userDTO: UserDTO,
                   authentication: Authentication): ResponseEntity<Any> {
        if (getUser(authentication).id != userId)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val updatedUser = userService.update(id = userId,
                user = User(email = userDTO.email, userPassword = userDTO.password))
        val userResponseDTO = UserResponseDTO(updatedUser.id, updatedUser.email)
        return ResponseEntity.ok(userResponseDTO)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable(value = "id") userId: UUID, authentication: Authentication): ResponseEntity<Any> {
        if (getUser(authentication).id != userId)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        userService.deleteById(id = userId)
        return ResponseEntity.noContent().build()
    }

    private fun getUser(authentication: Authentication) =
            authentication.principal as User


}

