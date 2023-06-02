package dev.janssenbatista.shopping.list.api.services

import dev.janssenbatista.shopping.list.api.exceptions.UnauthorizedException
import dev.janssenbatista.shopping.list.api.exceptions.UserAlreadyExistsException
import dev.janssenbatista.shopping.list.api.exceptions.UserNotFoundException
import dev.janssenbatista.shopping.list.api.models.User
import dev.janssenbatista.shopping.list.api.repositories.UserRepository
import dev.janssenbatista.shopping.list.api.secutiry.AuthenticationFacade
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class UserServiceUnitTest {

    private val userRepository = mockk<UserRepository>()
    private val authenticationFacade = mockk<AuthenticationFacade>()
    private lateinit var userService: UserService
    private lateinit var user: User

    @BeforeEach
    fun setup() {
        userService = UserService(userRepository, authenticationFacade)
        user = User(email = "user@email.com",
                userPassword = Random.nextBytes(8).toString())
    }

    // Find user

    @Test
    fun `should be able to find a user by id`() {
        val userId = UUID.randomUUID()
        every { authenticationFacade.getAuthentication().name } returns
                user.email
        every { userRepository.findById(userId) } returns Optional.of(user)
        val foundUser = userService.findUserById(userId)
        assertThat(foundUser).isEqualTo(user)
        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `should throw UserNotFoundException when id is invalid`() {
        every { userRepository.findById(user.id) } returns Optional.empty()
        assertThatExceptionOfType(UserNotFoundException::class.java).isThrownBy {
            userService.findUserById(user.id)
        }.withMessage("User with id ${user.id} not found")
        verify(exactly = 1) { userRepository.findById(user.id) }
        verify(exactly = 0) { authenticationFacade.getAuthentication().name }
    }

    @Test
    fun `should not be able to access data of other user`() {
        val userId = UUID.randomUUID()
        every { authenticationFacade.getAuthentication().name } returns
                "other@email.com"
        every { userRepository.findById(userId) } returns Optional.of(user)
        assertThatExceptionOfType(UnauthorizedException::class.java).isThrownBy {
            userService.findUserById(userId)
        }
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 1) { authenticationFacade.getAuthentication().name }
    }

    // Create user

    @Test
    fun `should be able to create a user`() {
        every { userRepository.save(user) } returns User(
                id = user.id,
                email = user.email,
                userPassword = BCryptPasswordEncoder(4).encode(user.password),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
        )
        every { userRepository.findByEmail(user.email) } returns Optional.empty()
        val createdUser = userService.save(user)
        assertEquals(user.id, createdUser.id)
        assertNotEquals(user.password, createdUser.password)
        verify(exactly = 1) { userRepository.findByEmail(user.email) }
        verify(exactly = 1) { userRepository.save(user) }
    }

    @Test
    fun `should not be able to create a user with existing email`() {
        every { userRepository.findByEmail(user.email) } returns Optional.of(user)
        assertThatExceptionOfType(UserAlreadyExistsException::class.java).isThrownBy {
            userService.save(user)
        }.withMessage("User already exists!")
        verify(exactly = 1) { userRepository.findByEmail(user.email) }
        verify(exactly = 0) { userRepository.save(user) }
    }

    // Update user

    @Test
    fun `should be able to update a user`() {
        every { userRepository.findById(user.id) } returns Optional.of(user)
        every { authenticationFacade.getAuthentication().name } returns user.email
        val updatedUser = User(
                id = user.id,
                email = Random.nextBytes(100).toString(),
                userPassword = BCryptPasswordEncoder().encode(Random.nextBytes(8).toString()),
                createdAt = user.createdAt,
                updatedAt = LocalDateTime.now().plusMinutes(10),
        )
        every { userRepository.save(user) } returns updatedUser
        userService.update(id = user.id, user = user)
        assertThat(user.id).isEqualTo(updatedUser.id)
        assertThat(user.email).isNotEqualTo(updatedUser.email)
        assertThat(user.password).isNotEqualTo(updatedUser.password)
        assertThat(user.createdAt).isEqualTo(updatedUser.createdAt)
        assertThat(user.updatedAt).isNotEqualTo(updatedUser.updatedAt)
        verify(exactly = 1) { userRepository.findById(user.id) }
        verify(exactly = 1) { authenticationFacade.getAuthentication().name }
        verify(exactly = 1) { userRepository.save(user) }
    }

    @Test
    fun `should not be able to update a nonexistent user`() {
        every { userRepository.findById(user.id) } returns Optional.empty()
        assertThatExceptionOfType(UserNotFoundException::class.java).isThrownBy {
            userService.update(id = user.id, user = user)
        }.withMessage("User with id ${user.id} not found")
        verify(exactly = 1) { userRepository.findById(user.id) }
        verify(exactly = 0) { authenticationFacade.getAuthentication().name }
        verify(exactly = 0) { userRepository.save(user) }
    }

    @Test
    fun `should not be able to update data of other user`() {
        every { userRepository.findById(user.id) } returns Optional.of(user)
        every { authenticationFacade.getAuthentication().name } throws UnauthorizedException()
        assertThatExceptionOfType(UnauthorizedException::class.java).isThrownBy {
            userService.update(user.id, user)
        }
        verify(exactly = 1) { userRepository.findById(user.id) }
        verify(exactly = 1) { authenticationFacade.getAuthentication().name }
        verify(exactly = 0) { userRepository.save(user) }
    }

    // Delete user

    @Test
    fun `should be able to delete a user`() {
        every { userRepository.findById(user.id) } returns Optional.of(user)
        every { authenticationFacade.getAuthentication().name } returns user.email
        every { userRepository.deleteById(user.id) } returns Unit
        userService.deleteById(user.id)
        verify(exactly = 1) { userRepository.findById(user.id) }
        verify(exactly = 1) { authenticationFacade.getAuthentication().name }
        verify(exactly = 1) { userRepository.deleteById(user.id) }
    }

    @Test
    fun `should not be able to delete a nonexistent user`() {
        val id = UUID.randomUUID()
        every { userRepository.findById(id) } returns Optional.empty()
        assertThatExceptionOfType(UserNotFoundException::class.java).isThrownBy {
            userService.deleteById(id)
        }.withMessage("User with id $id not found")
        verify(exactly = 1) { userRepository.findById(id) }
        verify(exactly = 0) { authenticationFacade.getAuthentication().name }
        verify(exactly = 0) { userRepository.deleteById(id) }
    }

    @Test
    fun `should not be able to delete data of other user`() {
        every { userRepository.findById(user.id) } returns Optional.of(user)
        every { authenticationFacade.getAuthentication().name } returns "other@email.com"
        assertThatExceptionOfType(UnauthorizedException::class.java).isThrownBy {
            userService.deleteById(user.id)
        }
        verify(exactly = 1) { userRepository.findById(any()) }
        verify(exactly = 1) { authenticationFacade.getAuthentication().name }
        verify(exactly = 0) { userRepository.deleteById(any()) }
    }

    // Load user by email
    @Test
    fun `should load a user by email`() {
        every { userRepository.findByEmail(user.email) } returns Optional.of(user)
        val userDetails = userService.loadUserByUsername(user.email)
        assertThat(userDetails).isEqualTo(user)
    }

    @Test
    fun `should not load a user with nonexistent email`() {
        every { userRepository.findByEmail(user.email) } returns Optional.empty()
        assertThatExceptionOfType(UsernameNotFoundException::class.java).isThrownBy {
            userService.loadUserByUsername(user.email)
        }.withMessage("User not found")
    }

}