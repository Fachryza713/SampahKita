package com.sampahkita.app.data.repository

import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface UserRepository {
    val semuaUser: Flow<List<User>>

    fun user(id: String): Flow<User?>

    suspend fun getUser(id: String): User?

    /**
     * Ubah saldo dan total kg nasabah.
     * @param nominal positif untuk setoran, negatif untuk pencairan.
     */
    suspend fun ubahSaldo(userId: String, nominal: Long, tambahKg: Double = 0.0)
}

/** Data user di memori, diawali dari [DummyData]. */
class FakeUserRepository : UserRepository {

    private val users = MutableStateFlow(DummyData.akun.map { it.user }.associateBy { it.id })

    override val semuaUser: Flow<List<User>> = users.map { it.values.toList() }

    override fun user(id: String): Flow<User?> = users.map { it[id] }

    override suspend fun getUser(id: String): User? = users.value[id]

    override suspend fun ubahSaldo(userId: String, nominal: Long, tambahKg: Double) {
        users.update { map ->
            val u = map[userId] ?: return@update map
            map + (userId to u.copy(saldo = u.saldo + nominal, totalKg = u.totalKg + tambahKg))
        }
    }
}
