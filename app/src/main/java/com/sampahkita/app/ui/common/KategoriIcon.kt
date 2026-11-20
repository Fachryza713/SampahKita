package com.sampahkita.app.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/** Memetakan kunci [com.sampahkita.app.data.model.KategoriSampah.ikon] ke ikon Material. */
fun kategoriIcon(ikon: String): ImageVector = when (ikon) {
    "kardus" -> Icons.Outlined.Inventory2
    "botol" -> Icons.Outlined.LocalDrink
    "gelas" -> Icons.Outlined.LocalCafe
    "kertas" -> Icons.Outlined.Description
    "minyak" -> Icons.Outlined.WaterDrop
    else -> Icons.Outlined.Recycling // termasuk "kaleng"
}
