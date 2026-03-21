package com.subia.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subia.android.ui.ServiceLogo
import com.subia.android.ui.components.GastosPorCategoriaCard
import com.subia.android.ui.theme.GradientAmberEnd
import com.subia.android.ui.theme.GradientAmberStart
import com.subia.android.ui.theme.GradientIndigoEnd
import com.subia.android.ui.theme.GradientIndigoStart
import com.subia.android.ui.theme.GradientTealEnd
import com.subia.android.ui.theme.GradientTealStart
import com.subia.android.ui.theme.Warning
import com.subia.shared.model.DashboardSummary
import com.subia.shared.model.ProximaRenovacion
import com.subia.shared.viewmodel.DashboardUiState
import com.subia.shared.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val totalesPorMoneda by viewModel.totalesPorMoneda.collectAsState()
    val gastosPorCategoria by viewModel.gastosPorCategoria.collectAsState()
    val isRefreshing = uiState is DashboardUiState.Loading

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refrescar() },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            is DashboardUiState.Success -> DashboardContent(state.resumen, totalesPorMoneda, gastosPorCategoria)
            is DashboardUiState.Offline -> Column {
                BannerOffline("Mostrando datos guardados — sin conexión")
                state.resumenCacheado?.let { DashboardContent(it, totalesPorMoneda, gastosPorCategoria) }
            }
            is DashboardUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.mensaje, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.cargarEstadisticas() }) { Text("Reintentar") }
                }
            }
            is DashboardUiState.SesionExpirada -> Unit
        }
    }
}

@Composable
private fun DashboardContent(
    resumen: DashboardSummary,
    totalesPorMoneda: Map<String, Double> = emptyMap(),
    gastosPorCategoria: Map<String, Double> = emptyMap()
) {
    val gradients = listOf(
        Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)),
        Brush.linearGradient(listOf(GradientTealStart, GradientTealEnd)),
        Brush.linearGradient(listOf(GradientAmberStart, GradientAmberEnd))
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Resumen de tus suscripciones",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            if (totalesPorMoneda.isEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.EuroSymbol,
                        label = "Mensual",
                        value = "%.2f €".format(resumen.gastoMensual),
                        gradient = gradients[0]
                    )
                    GradientStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CalendarToday,
                        label = "Anual",
                        value = "%.0f €".format(resumen.gastoAnual),
                        gradient = gradients[1]
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    totalesPorMoneda.entries.toList().chunked(2).forEachIndexed { rowIdx, rowEntries ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowEntries.forEachIndexed { colIdx, (moneda, total) ->
                                val gradientIdx = (rowIdx * 2 + colIdx) % gradients.size
                                GradientStatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.EuroSymbol,
                                    label = "Mensual ($moneda)",
                                    value = "%.2f %s".format(total, moneda),
                                    gradient = gradients[gradientIdx]
                                )
                            }
                            if (rowEntries.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Subscriptions,
                        null,
                        tint = Warning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Suscripciones activas",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${resumen.totalSuscripciones}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Warning
                    )
                }
            }
        }

        if (resumen.renovacionesProximas.isNotEmpty()) {
            item {
                Text(
                    "Próximas renovaciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(resumen.renovacionesProximas) { RenovacionCard(it) }
        } else {
            item {
                Text(
                    "No tienes renovaciones próximas",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            GastosPorCategoriaCard(gastosPorCategoria = gastosPorCategoria)
        }
    }
}

@Composable
private fun GradientStatCard(modifier: Modifier, icon: ImageVector, label: String, value: String, gradient: Brush) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
    }
}

@Composable
private fun RenovacionCard(renovacion: ProximaRenovacion) {
    val diasColor = when {
        renovacion.diasRestantes <= 3 -> MaterialTheme.colorScheme.error
        renovacion.diasRestantes <= 7 -> Warning
        else -> Color(0xFF22C55E)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            ServiceLogo(nombre = renovacion.nombre, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(renovacion.nombre, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(
                    renovacion.fechaRenovacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "%.2f €".format(renovacion.precio),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${renovacion.diasRestantes}d",
                    style = MaterialTheme.typography.bodySmall,
                    color = diasColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun BannerOffline(mensaje: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Warning)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        Alignment.Center
    ) {
        Text(mensaje, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}
