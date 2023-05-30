package dev.janssenbatista.shopping.list.api.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.Hibernate
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "tb_users")
data class User(
        @Id
        @Column(length = 36)
        @GeneratedValue(strategy = GenerationType.UUID)
        val id: UUID = UUID.randomUUID(),
        @Column(nullable = false, unique = true, length = 100)
        var email: String,
        @Column(nullable = false, length = 60, name = "password")
        @JsonIgnore
        var userPassword: String,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        var updatedAt: LocalDateTime = LocalDateTime.now()
) : Serializable, UserDetails {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as User

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id )"
    }

    @JsonIgnore
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
            mutableListOf<GrantedAuthority>(SimpleGrantedAuthority("USER"))

    @JsonIgnore
    override fun getPassword(): String = this.userPassword

    @JsonIgnore
    override fun getUsername() = this.email

    @JsonIgnore
    override fun isAccountNonExpired() = true

    @JsonIgnore
    override fun isAccountNonLocked() = true

    @JsonIgnore
    override fun isCredentialsNonExpired() = true

    @JsonIgnore
    override fun isEnabled() = true
}
