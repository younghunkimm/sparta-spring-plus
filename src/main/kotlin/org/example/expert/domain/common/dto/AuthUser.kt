package org.example.expert.domain.common.dto

import org.example.expert.domain.user.enums.UserRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

data class AuthUser(
    val id: Long,
    val email: String,
    val nickname: String,
    val userRole: UserRole
) {

    fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(userRole.name))
    }
}