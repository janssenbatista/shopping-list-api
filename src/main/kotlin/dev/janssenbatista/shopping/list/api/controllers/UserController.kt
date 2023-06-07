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
    fun createUser(@RequestBody @Valid userDTO: UserDTO): ResponseEntity<User> {
        val createdUser = userService.save(User(email = userDTO.email, userPassword = userDTO.password))
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @GetMapping("/{id}")
    fun findUserById(@PathVariable(value = "id") userId: UUID): ResponseEntity<User> {
        val user = userService.findUserById(id = userId)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable(value = "id") userId: UUID,
                   @RequestBody @Valid userDTO: UserDTO): ResponseEntity<User> {
        val updatedUser = userService.update(id = userId,
                user = User(email = userDTO.email, userPassword = userDTO.password))
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable(value = "id") userId: UUID): ResponseEntity<Any> {
        userService.deleteById(id = userId)
        return ResponseEntity.noContent().build()
    }


}

