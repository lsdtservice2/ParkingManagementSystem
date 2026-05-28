package com.logisparktech.parkingmanagementsystem.presentation.recent_tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.logisparktech.parkingmanagementsystem.R
import com.logisparktech.parkingmanagementsystem.data.local.entities.TicketEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun RecentTicketsScreen(
    viewModel: RecentTicketsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tickets by viewModel.tickets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showReprintDialog by remember { mutableStateOf(false) }
    var selectedTicket by remember { mutableStateOf<TicketEntity?>(null) }
    val rateTypeMap by viewModel.rateTypeMap.collectAsState()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.refreshTickets() }
    )

    LaunchedEffect(Unit) {
        viewModel.refreshTickets()
        viewModel.syncEvent.collect { result ->
            when (result) {
                is SyncResult.Success -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.sync_success))
                }
                is SyncResult.Error -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.sync_failed, result.message))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.recent_tickets),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.syncTickets() },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = stringResource(R.string.sync_tickets),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                modifier = Modifier
                    .height(64.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            if (tickets.isEmpty() && !isLoading) {
                EmptyTicketsView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tickets) { ticket ->
                        val vehicleType = rateTypeMap[ticket.rateId] ?: ""
                        TicketCard(
                            ticket = ticket,
                            vehicleType = vehicleType,
                            onClick = {
                                if (!ticket.isClosed) {
                                    selectedTicket = ticket
                                    showReprintDialog = true
                                }
//                                else {
//                                    viewModel.reprintTicket(ticket)
//                                }
                            }
                        )
                    }
                }
            }

            if (showReprintDialog && selectedTicket != null) {
                AlertDialog(
                    onDismissRequest = { showReprintDialog = false },
                    title = {
                        Text(
                            text = stringResource(R.string.reprint_ticket),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    text = {
                        Text(stringResource(R.string.reprint_confirm_msg, selectedTicket?.vehicleNumber ?: ""))
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedTicket?.let { viewModel.reprintTicket(it) }
                                showReprintDialog = false
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.reprint))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReprintDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketCard(ticket: TicketEntity,vehicleType: String, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault())
    val icon = when (vehicleType.lowercase()) {
        "car", "four wheeler", "4 wheeler" -> Icons.Default.DirectionsCar
        "bike", "two wheeler", "2 wheeler", "cycle" -> Icons.Default.TwoWheeler
        "auto", "rickshaw" -> Icons.Default.ElectricRickshaw
        "bus", "truck", "heavy" -> Icons.Default.BusAlert
        else -> if (ticket.isClosed) Icons.Default.CheckCircle else Icons.Default.DirectionsCar
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (ticket.isClosed) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
//                    imageVector = if (ticket.isClosed) Icons.Default.CheckCircle else Icons.Default.DirectionsCar,
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (ticket.isClosed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticket.vehicleNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = stringResource(R.string.in_label, sdf.format(Date(ticket.entryTime))),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (ticket.isClosed && ticket.exitTime != null) {
                    Text(
                        text = stringResource(R.string.out_label, sdf.format(Date(ticket.exitTime))),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (ticket.isClosed) {
                    Text(
                        text = stringResource(R.string.amount_format, ticket.amount.toInt()),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                    Surface(
                        color = if (ticket.isSynced) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(
                            0xFFFF9800
                        ).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (ticket.isSynced) stringResource(R.string.status_synced) else stringResource(R.string.status_pending),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (ticket.isSynced) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.status_active),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTicketsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_tickets_found),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
