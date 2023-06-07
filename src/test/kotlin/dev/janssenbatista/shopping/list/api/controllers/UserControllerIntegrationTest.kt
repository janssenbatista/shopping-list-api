package dev.janssenbatista.shopping.list.api.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import dev.janssenbatista.shopping.list.api.models.User
import dev.janssenbatista.shopping.list.api.models.dtos.UserDTO
import dev.janssenbatista.shopping.list.api.repositories.UserRepository
import dev.janssenbatista.shopping.list.api.services.UserService
import dev.janssenbatista.shopping.list.api.utils.format
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Clock
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    @Autowired
    private lateinit var userController: UserController

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val email = "user@email.com"
    private val password = Random.nextBytes(8).toString()

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    // create user

    @Test
    fun `should be able to create a user and return status code 201`() {
        val userDto = UserDTO(email = "user@email.com", password = "12345678")
        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto))
        )
                .andExpect(MockMvcResultMatchers.status().isCreated)
    }

    @Test
    fun `should not be able to create a user with an existing email and return status code 409`() {
        val email = email
        val password = password
        userRepository.save(User(email = email, userPassword = password))
        val userDto = UserDTO(email = email, password = password)
        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto))
        )
                .andExpect(MockMvcResultMatchers.status().isConflict)
    }

    // find user by id

    @Test
    @WithMockUser(username = "user@email.com")
    fun `should be able to find a user by email and return status code 200`() {
        val id = UUID.randomUUID()
        val user = User(
                id = id,
                email = email,
                userPassword = password,
                createdAt = LocalDateTime.now(Clock.systemUTC()),
                updatedAt = LocalDateTime.now()
        )
        val createdUser = userRepository.save(user)

        mockMvc
                .perform(get("/users/${createdUser.id}"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("${createdUser.id}"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(createdUser.email))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.createdAt")
                                .value(createdUser.createdAt.format())
                )
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.updatedAt")
                                .value(createdUser.updatedAt.format())
                )

    }

    @Test
    @WithMockUser("otheruser@email.com")
    fun `should return status code 401 when authenticated user try access data of other user`() {
        val user = User(
                email = email,
                userPassword = password,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
        )
        val user2 = User(
                email = "otheruser@email.com",
                userPassword = password,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
        )
        userRepository.saveAll(listOf(user, user2))
        mockMvc.perform(get("$BASE_REQUEST_URI/${user.id}"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `should return status code 401 when user is not authenticated`() {
        mockMvc.perform(get("$BASE_REQUEST_URI/{id}", UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    // update user

    @Test
    @WithMockUser(username = "user@email.com")
    fun `should be able to update a user and return status code 200`() {
        val user = User(email = email, userPassword = password)
        userService.save(user)
        val newEmail = "new@email.com"
        val updateUserDTO = UserDTO(email = newEmail, password = user.userPassword)
        val userDtoAsString = objectMapper.writeValueAsString(updateUserDTO)
        mockMvc.perform(put("$BASE_REQUEST_URI/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userDtoAsString))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(user.id.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(newEmail))
    }

    @Test
    @WithMockUser
    fun `should not be able to update a user with invalid id and return status code 404`() {
        val id = UUID.randomUUID()
        val updateUserDTO = UserDTO(email = email, password = password)
        val userDtoAsString = objectMapper.writeValueAsString(updateUserDTO)
        val response = mockMvc.perform(put("$BASE_REQUEST_URI/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userDtoAsString))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andReturn()
        Assertions.assertThat(response.response.contentAsString)
                .isEqualTo("User with id $id not found")
    }

    @Test
    @WithMockUser(username = "other@email.com")
    fun `should not be able to update data of other user and return status code 401`() {
        val createdUser = userService.save(User(email = email, userPassword = password))
        val updateUserDTO = UserDTO(email = email, password = password)
        val userDtoAsString = objectMapper.writeValueAsString(updateUserDTO)
        mockMvc.perform(put("$BASE_REQUEST_URI/${createdUser.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userDtoAsString))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    // delete user

    @Test
    @WithMockUser(username = "user@email.com")
    fun `should be able to delete a user and return status code 204`() {
        val user = User(
                email = email,
                userPassword = password
        )
        val createdUser = userRepository.save(user)
        mockMvc.perform(delete("$BASE_REQUEST_URI/${createdUser.id}"))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @WithMockUser
    fun `should not be able to delete a non-existing user and return status code 404`() {
        mockMvc.perform(delete("$BASE_REQUEST_URI/${UUID.randomUUID()}"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @WithMockUser(username = "other@email.com")
    fun `should not be able to delete data of other user and return status code 401`() {
        val user = User(
                email = email,
                userPassword = password
        )
        val createdUser = userRepository.save(user)
        mockMvc.perform(delete("$BASE_REQUEST_URI/${createdUser.id}"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    companion object {

        private const val BASE_REQUEST_URI = "/users"

        @Container
        @JvmStatic
        val container: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:latest")
                .withUsername("root")
                .withPassword("password")

        @DynamicPropertySource
        @JvmStatic
        fun overrideProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.username", container::getUsername)
            registry.add("spring.datasource.password", container::getPassword)
        }
    }

}