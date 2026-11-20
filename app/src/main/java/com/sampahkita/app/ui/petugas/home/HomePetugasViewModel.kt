package com.sampahkita.app.ui.petugas.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sampahkita.app.SampahKitaApp
import com.sampahkita.app.data.model.BankSampah
import com.sampahkita.app.data.model.User
import com.sampahkita.app.ui.petugas.PengajuanItem
import com.sampahkita.app.ui.petugas.StatHarian
import com.sampahkita.app.ui.petugas.pengajuanMenunggu
import com.sampahkita.app.ui.petugas.statHarian
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomePetugasUiState(
    val petugas: User? = null,
    val unit: BankSampah? = null,
    val stat: StatHarian = StatHarian(),
    /** Semua pengajuan menunggu (layar menampilkan maks 3). */
    val pengajuan: List<PengajuanItem> = emptyList(),
)

class HomePetugasViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SampahKitaApp).container
    private val banks = MutableStateFlow<List<BankSampah>>(emptyList())

    init {
        viewModelScope.launch { banks.value = container.bankSampahRepository.getBankSampah() }
    }

    val uiState: StateFlow<HomePetugasUiState> = combine(
        container.authRepository.currentUser,
        container.transaksiRepository.riwayat,
        container.userRepository.semuaUser,
        banks,
    ) { petugas, transaksi, users, bankList ->
        HomePetugasUiState(
            petugas = petugas,
            unit = bankList.firstOrNull { it.id == petugas?.bankSampahId },
            stat = statHarian(transaksi, petugas?.bankSampahId, LocalDate.now()),
            pengajuan = pengajuanMenunggu(transaksi, users, bankList),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomePetugasUiState())
}
