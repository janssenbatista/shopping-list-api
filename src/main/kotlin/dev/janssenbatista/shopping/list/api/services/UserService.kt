package dev.janssenbatista.shopping.list.api.services

import dev.janssenbatista.shopping.list.api.exceptions.UserAlreadyExistsException
import dev.janssenbatista.shopping.list.api.exceptions.UserNotFoundException
import dev.janssenbatista.shopping.list.api.models.User
import dev.janssenbatista.shopping.list.api.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(private val userRepository: UserRepository) : UserDetailsService {

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
        val hashPassword = BCryptPasswordEncoder(12).encode(user.password)
        userAlreadyExists.get().apply {
            email = user.email
            password = hashPassword
            updatedAt = LocalDateTime.now()
        }
        return userRepository.save(user)
    }

    fun deleteById(id: UUID) {
        userRepository.findById(id)
                .orElseThrow { throw UserNotFoundException("User with id $id not found") }
        userRepository.deleteById(id)
    }

    override fun loadUserByUsername(email: String?): UserDetails {
        return userRepository.findByEmail(email!!)
                .orElseThrow { throw UsernameNotFoundException("User not found") }
    }
}


