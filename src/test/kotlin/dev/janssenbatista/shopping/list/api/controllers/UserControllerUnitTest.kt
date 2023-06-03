package dev.janssenbatista.shopping.list.api.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import dev.janssenbatista.shopping.list.api.exceptions.UnauthorizedException
import dev.janssenbatista.shopping.list.api.exceptions.UserAlreadyExistsException
import dev.janssenbatista.shopping.list.api.exceptions.UserNotFoundException
import dev.janssenbatista.shopping.list.api.models.User
import dev.janssenbatista.shopping.list.api.models.dtos.UserDTO
import dev.janssenbatista.shopping.list.api.secutiry.WebSecurityConfig
import dev.janssenbatista.shopping.list.api.services.UserService
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime
import java.util.*


@WebMvcTest(UserController::class)
@Import(WebSecurityConfig::class)
class UserControllerUnitTest {

    private lateinit var userDTO: UserDTO

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @MockkBean
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userDTO = UserDTO(
                email = "user@email.com",
                password = "user123456"
        )
    }

    // find user by id

    @Test
    @WithMockUser
    fun `should be able to find a user and return status code 200`() {
        val userId = UUID.randomUUID()
        val user = buildCreatedUser(id = userId)
        every { userService.findUserById(userId) } returns user
        mockMvc.perform(get("$REQUEST_BASE_URL/$userId")
        ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user.email))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value("${user.createdAt}"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.updatedAt").value("${user.updatedAt}"))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    @WithMockUser
    fun `should not be able to find a user with invalid id and return status code 404`() {
        val userId = UUID.randomUUID()
        every { userService.findUserById(any()) } throws UserNotFoundException("User with id $userId not found")
        val response = mockMvc.perform(get("$REQUEST_BASE_URL/$userId"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andReturn()
        assertThat(response.response.contentAsString).isEqualTo("User with id $userId not found")

    }

    @Test
    @WithMockUser
    fun `should not be able to access info of other user and return status code 401`() {
        val userId = UUID.randomUUID()
        every { userService.findUserById(userId) } throws UnauthorizedException()
        mockMvc.perform(get("$REQUEST_BASE_URL/$userId")
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andDo(MockMvcResultHandlers.print())
    }

    // Create user

    @Test
    fun `should create a user and return status code 201`() {
        val dtoAsString = mapper.writeValueAsString(userDTO)
        every { userService.save(any()) } returns buildCreatedUser()
        mockMvc
                .perform(post(REQUEST_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoAsString))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not be able to create a user with existing email and return status code 409`() {
        every { userService.save(any()) } throws UserAlreadyExistsException("User already exists")
        val dtoAsString = mapper.writeValueAsString(userDTO)
        val response = mockMvc.perform(post(REQUEST_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dtoAsString))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andReturn()
        assertThat(response.response.contentAsString).isEqualTo("User already exists")
    }

    // update user

    @Test
    @WithMockUser
    fun `should be able to update a user and return status code 200`() {
        val userDTO = UserDTO(email = "other@email.com",
                password = "12345678")
        val jsonAsString = mapper.writeValueAsString(userDTO)
        val updatedUser = User(UUID.randomUUID(), email = userDTO.email, userPassword = userDTO.password,
                updatedAt = LocalDateTime.now().plusMinutes(1))
        every { userService.update(any(), any()) } returns updatedUser
        mockMvc.perform(put("$REQUEST_BASE_URL/${updatedUser.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAsString))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("${updatedUser.id}"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(updatedUser.email))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value("${updatedUser.createdAt}"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.updatedAt").value("${updatedUser.updatedAt}"))
    }

    @Test
    @WithMockUser
    fun `should not to be able to update a user with invalid email and return status code 404`() {
        val userId = UUID.randomUUID()
        val dtoAsString = mapper.writeValueAsString(userDTO)
        every { userService.update(userId, any()) } throws UserNotFoundException("User with id $userId not found")
        val response = mockMvc.perform(put("$REQUEST_BASE_URL/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dtoAsString)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
                .andReturn()
        assertThat(response.response.contentAsString).isEqualTo("User with id $userId not found")
    }

    @Test
    @WithMockUser
    fun `should not to be able to update data of other user and return status code 401`() {
        val userId = UUID.randomUUID()
        val dtoAsString = mapper.writeValueAsString(userDTO)
        every { userService.update(userId, any()) } throws UnauthorizedException()
        mockMvc.perform(put("$REQUEST_BASE_URL/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dtoAsString)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `should be able to delete a user and return status code 204`() {
        val userId = UUID.randomUUID()
        every { userService.deleteById(userId) } returns Unit
        mockMvc.perform(delete("$REQUEST_BASE_URL/${userId}"))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    @WithMockUser
    fun `should not to be able to update a user with invalid id and return status code 404`() {
        val userId = UUID.randomUUID()
        every { userService.deleteById(userId) } throws UserNotFoundException("User with id $userId not found")
        val response = mockMvc.perform(delete("$REQUEST_BASE_URL/$userId")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
                .andReturn()
        assertThat(response.response.contentAsString).isEqualTo("User with id $userId not found")
    }

    @Test
    @WithMockUser
    fun `should not to be able to delete data of other user and return status code 401`() {
        val userId = UUID.randomUUID()
        every { userService.deleteById(userId) } throws UnauthorizedException()
        mockMvc.perform(delete("$REQUEST_BASE_URL/$userId")
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    private fun buildCreatedUser(id: UUID = UUID.randomUUID()) = User(
            id = id,
            email = userDTO.email,
            userPassword = BCryptPasswordEncoder(4).encode(userDTO.password),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
    )

    companion object {
        const val REQUEST_BASE_URL = "/users"
    }
}