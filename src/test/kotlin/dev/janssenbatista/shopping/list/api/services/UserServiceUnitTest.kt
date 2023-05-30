package dev.janssenbatista.shopping.list.api.services

import dev.janssenbatista.shopping.list.api.exceptions.UserAlreadyExistsException
import dev.janssenbatista.shopping.list.api.exceptions.UserNotFoundException
import dev.janssenbatista.shopping.list.api.models.User
import dev.janssenbatista.shopping.list.api.repositories.UserRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class UserServiceUnitTest {

    private val userRepository = mockk<UserRepository>()
    private lateinit var userService: UserService
    private lateinit var user: User

    @BeforeEach
    fun setup() {
        userService = UserService(userRepository)
        user = User(email = Random.nextBytes(8).toString(),
                password = Random.nextBytes(8).toString())
    }

    // Find user

    @Test
    fun `should be able to find a user by id`() {
        val userId = UUID.randomUUID()
        every { userRepository.findById(userId) } returns Optional.of(user)
        val foundUser = userService.findUserById(userId)
        assertEquals(foundUser, user)
        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `should throw UserNotFoundException when id is invalid`() {
        every { userRepository.findById(user.id) } returns Optional.empty()
        assertThrows(UserNotFoundException::class.java) {
            userService.findUserById(user.id)
        }
        verify(exactly = 1) { userRepository.findById(user.id) }
    }

    // Create user

    @Test
    fun `should be able to create a user`() {
        every { userRepository.save(user) } returns User(
                id = user.id,
                email = user.email,
                password = BCryptPasswordEncoder(4).encode(user.password),
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
        assertThrows(UserAlreadyExistsException::class.java) {
            userService.save(user)
        }
        verify(exactly = 0) { userRepository.save(user) }
    }

    // Update user

    @Test
    fun `should be able to update a user`() {
        every { userRepository.findById(user.id) } returns Optional.of(user)
        val updatedUser = User(
                id = user.id,
                email = Random.nextBytes(100).toString(),
                password = BCryptPasswordEncoder().encode(Random.nextBytes(8).toString()),
                createdAt = user.createdAt,
                updatedAt = LocalDateTime.now().plusMinutes(10),
        )
        every { userRepository.save(user) } returns updatedUser
        userService.update(id = user.id, user = user)
        assertEquals(user.id, updatedUser.id)
        assertNotEquals(user.email, updatedUser.email)
        assertNotEquals(user.password, updatedUser.password)
        assertEquals(user.createdAt, updatedUser.createdAt)
        assertNotEquals(user.updatedAt, updatedUser.updatedAt)
        verify(exactly = 1) { userRepository.save(user) }
    }

    @Test
    fun `should not be able to update a nonexistent user`() {
        every { userRepository.findById(user.id) } returns Optional.empty()
        val exception = assertThrows(UserNotFoundException::class.java) {
            userService.update(id = user.id, user = user)
        }
        assertEquals("User with id ${user.id} not found", exception.message)
        verify(exactly = 0) { userRepository.save(user) }
    }

    // Delete user

    @Test
    fun `should be able to delete a user`() {
        every { userRepository.findById(user.id) } returns Optional.of(user)
        every { userRepository.deleteById(user.id) } returns Unit
        userService.deleteById(user.id)
        verify(exactly = 1) { userRepository.findById(user.id) }
        verify(exactly = 1) { userRepository.deleteById(user.id) }
    }

    @Test
    fun `should not be able to delete a nonexistent user`() {
        val id = UUID.randomUUID()
        every { userRepository.findById(id) } returns Optional.empty()
        val exception = assertThrows(UserNotFoundException::class.java) {
            userService.deleteById(id)
        }
        assertEquals("User with id $id not found", exception.message)
        verify(exactly = 1) { userRepository.findById(id) }
        verify(exactly = 0) { userRepository.deleteById(id) }
    }


}