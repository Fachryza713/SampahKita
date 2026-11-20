package com.sampahkita.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sampahkita.app.data.dummy.DummyData
import com.sampahkita.app.data.model.User

/**
 * Login sementara: dua tombol untuk masuk sebagai akun demo.
 * TODO: diganti form login lengkap di fitur 03.
 */
@Composable
fun LoginScreen(onLoginBerhasil: (User) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Masuk",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Form login segera hadir. Pilih akun demo:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onLoginBerhasil(DummyData.nasabah) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Masuk sebagai Nasabah") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onLoginBerhasil(DummyData.petugas) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Masuk sebagai Petugas") }
    }
}
