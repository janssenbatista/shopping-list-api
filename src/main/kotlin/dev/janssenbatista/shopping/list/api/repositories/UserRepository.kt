package dev.janssenbatista.shopping.list.api.repositories

import dev.janssenbatista.shopping.list.api.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {

    fun findByEmail(email: String): Optional<User>

}