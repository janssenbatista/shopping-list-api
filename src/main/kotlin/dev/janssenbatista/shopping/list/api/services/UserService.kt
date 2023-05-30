package dev.janssenbatista.shopping.list.api.services

import dev.janssenbatista.shopping.list.api.exceptions.UserAlreadyExistsException
import dev.janssenbatista.shopping.list.api.exceptions.UserNotFoundException
import dev.janssenbatista.shopping.list.api.models.User
import dev.janssenbatista.shopping.list.api.repositories.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(private val userRepository: UserRepository) {

    fun findUserById(id: UUID): User {
        return userRepository.findById(id).orElseThrow {
            throw UserNotFoundException("User with id $id not found")
        }
    }

    fun save(user: User): User {
        val userAlreadyExists = userRepository.findByEmail(user.email)
        if (userAlreadyExists.isPresent) {
            throw UserAlreadyExistsException("User already exists!")
        }
        val hashPassword = BCryptPasswordEncoder(12).encode(user.password)
        user.password = hashPassword
        return userRepository.save(user)
    }

    fun update(id: UUID, user: User): User {
        val userAlreadyExists = userRepository.findById(id)
        if (!userAlreadyExists.isPresent) {
            throw UserNotFoundException("User with id $id not found")
        }
        val hashPassword = BCryptPasswordEncoder().encode(user.password)
        user.password = hashPassword
        user.updatedAt = LocalDateTime.now()
        return userRepository.save(user)
    }

    fun deleteById(userId: UUID) {
        userRepository.findById(userId)
                .orElseThrow { throw UserNotFoundException("User with id $userId not found") }
        userRepository.deleteById(userId)
    }
}


