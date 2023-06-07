package dev.janssenbatista.shopping.list.api.services

import dev.janssenbatista.shopping.list.api.exceptions.UnauthorizedException
import dev.janssenbatista.shopping.list.api.exceptions.UserAlreadyExistsException
import dev.janssenbatista.shopping.list.api.exceptions.UserNotFoundException
import dev.janssenbatista.shopping.list.api.models.User
import dev.janssenbatista.shopping.list.api.repositories.UserRepository
import dev.janssenbatista.shopping.list.api.secutiry.AuthenticationFacade
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class UserService(private val userRepository: UserRepository,
                  private val authenticationFacade: AuthenticationFacade) : UserDetailsService {

    fun findUserById(id: UUID): User {
        val user = userRepository.findById(id).orElseThrow {
            throw UserNotFoundException("User with id $id not found")
        }
        if (user.email != authenticationFacade.getAuthentication().name)
            throw UnauthorizedException()
        return user
    }

    fun save(user: User): User {
        val userAlreadyExists = userRepository.findByEmail(user.email)
        if (userAlreadyExists.isPresent) {
            throw UserAlreadyExistsException("User already exists!")
        }
        val hashPassword = BCryptPasswordEncoder(12).encode(user.userPassword)
        user.userPassword = hashPassword
        return userRepository.save(user)
    }

    fun update(id: UUID, user: User): User {
        val userExists = userRepository.findById(id).orElseThrow {
            UserNotFoundException("User with id $id not found")
        }
        if (userExists.email != authenticationFacade.getAuthentication().name)
            throw UnauthorizedException()
        val hashPassword = BCryptPasswordEncoder(12).encode(user.userPassword)
        userExists.apply {
            email = user.email
            userPassword = hashPassword
            updatedAt = LocalDateTime.now()
        }
        return userRepository.save(userExists)
    }

    fun deleteById(id: UUID) {
        val user = userRepository.findById(id)
                .orElseThrow { throw UserNotFoundException("User with id $id not found") }
        if (user.email != authenticationFacade.getAuthentication().name)
            throw UnauthorizedException()
        userRepository.deleteById(id)
    }

    override fun loadUserByUsername(email: String?): UserDetails {
        return userRepository.findByEmail(email!!)
                .orElseThrow { throw UsernameNotFoundException("User not found") }
    }

}


