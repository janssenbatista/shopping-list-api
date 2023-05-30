package dev.janssenbatista.shopping.list.api.controllers

import dev.janssenbatista.shopping.list.api.models.User
import dev.janssenbatista.shopping.list.api.models.dtos.UserDTO
import dev.janssenbatista.shopping.list.api.services.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun createUser(@RequestBody @Valid userDTO: UserDTO): ResponseEntity<Any> {
        val user = User(email = userDTO.email, password = userDTO.password)
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user))
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable(value = "id") userId: UUID): ResponseEntity<User> =
            ResponseEntity.ok(userService.findUserById(userId))

    @PutMapping("/{id}")
    fun updateUser(@PathVariable(value = "id") userId: UUID, @RequestBody @Valid userDTO: UserDTO): ResponseEntity<User> {
        val user = User(email = userDTO.email, password = userDTO.password)
        return ResponseEntity.ok(userService.update(id = userId, user = user))
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable(value = "id") userId: UUID): ResponseEntity.HeadersBuilder<*> {
        userService.deleteById(id = userId)
        return ResponseEntity.noContent()
    }

}