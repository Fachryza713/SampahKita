package com.sampahkita.app.data.model

enum class Role { NASABAH, PETUGAS }

data class User(
    val id: String,
    val nama: String,
    val username: String,
    val email: String,
    val role: Role,
    val saldo: Long,
    val totalKg: Double,
)
