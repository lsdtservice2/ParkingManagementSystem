package com.logisparktech.parkingmanagementsystem.presentation.entry

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.logisparktech.parkingmanagementsystem.data.local.entities.RateEntity
import kotlin.text.filter
import kotlin.text.isLetterOrDigit
import kotlin.text.isWhitespace


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    viewModel: EntryViewModel,
    onTicketGenerated: () -> Unit
) {
    val rates by viewModel.rates.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var vehicleNumber by remember { mutableStateOf("") }
    var selectedRate by remember { mutableStateOf<RateEntity?>(null) }
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is EntryViewModel.EntryUiState.Success) {
            vehicleNumber = ""
            selectedRate = null
            viewModel.resetState()
            onTicketGenerated()
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalParking,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Vehicle Entry",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .height(64.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        )
                    )
                )
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshRates() },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        SyncWarningCard(unsyncedCount = unsyncedCount)
//                        if (unsyncedCount <= 900) {
////                            Text(
////                                text = "Warning: Sync limit reached ($unsyncedCount/1000 tickets). Please sync soon.",
////                                style = MaterialTheme.typography.labelLarge.copy(
////                                    color = MaterialTheme.colorScheme.error, // Red color for warning
////                                    fontWeight = FontWeight.Bold
////                                ),
////                                modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
////                            )
//
//                        }


                        // Main Entry Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Input Details",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.align(Alignment.Start)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = vehicleNumber,
                                    onValueChange = { input ->
                                        val filtered =
                                            input.filter { it.isLetterOrDigit() || it.isWhitespace() }
                                        vehicleNumber = filtered.uppercase()
                                    },
                                    label = { Text("Vehicle Number") },
                                    placeholder = { Text("e.g. MH 12 AB 1234") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ConfirmationNumber,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Characters,
                                        imeAction = ImeAction.Done
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.3f
                                        ),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.3f
                                        )
                                    )
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                Text(
                                    text = "Vehicle Category",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.align(Alignment.Start)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(bottom = 8.dp)
                                ) {
                                    items(rates) { rate ->
                                        VehicleTypeCard(
                                            rate = rate,
                                            isSelected = selectedRate == rate,
                                            onClick = { selectedRate = rate }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Action Section (Bottom anchored)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedVisibility(
                            visible = uiState is EntryViewModel.EntryUiState.Error,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            val errorMessage =
                                (uiState as? EntryViewModel.EntryUiState.Error)?.message ?: ""
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = errorMessage,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                selectedRate?.let {
                                    viewModel.createTicket(vehicleNumber, it.rateId)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            enabled = vehicleNumber.isNotBlank() && selectedRate != null && uiState !is EntryViewModel.EntryUiState.Loading,
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            if (uiState is EntryViewModel.EntryUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "GENERATE TICKET",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.2.sp
                                    )
                                )
                            }
                        }
                    }
                } // end Column
            } // end PullToRefreshBox
        }
    }
}


@Composable
fun VehicleTypeCard(
    rate: RateEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (rate.vehicleType.lowercase()) {
        "car", "four wheeler", "4 wheeler" -> Icons.Default.DirectionsCar
        "bike", "two wheeler", "2 wheeler", "cycle" -> Icons.Default.TwoWheeler
        "auto", "rickshaw" -> Icons.Default.ElectricRickshaw
        "bus", "truck", "heavy" -> Icons.Default.BusAlert
        else -> Icons.Default.DirectionsCar
    }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .width(100.dp)
            .height(110.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.5f
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = rate.vehicleType,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )

            Text(
                text = "रु.${rate.pricePerHour}/hr",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

//@Composable
//fun SyncWarningCard(unsyncedCount: Int) {
// AnimatedVisibility(
////        visible = unsyncedCount >= 900,
//        visible = unsyncedCount <= 900,
//        enter = expandVertically() + fadeIn(),
//        exit = shrinkVertically() + fadeOut()
//    ) {
//        Surface(
//            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
//            shape = RoundedCornerShape(20.dp),
//            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 4.dp)
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        imageVector = Icons.Default.SyncProblem,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.error,
//                        modifier = Modifier.size(24.dp)
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Text(
//                        text = "Storage Limit Reached",
//                        style = MaterialTheme.typography.titleMedium.copy(
//                            fontWeight = FontWeight.ExtraBold,
//                            color = MaterialTheme.colorScheme.onErrorContainer
//                        )
//                    )
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "You have $unsyncedCount/1000 unsynced tickets. Please sync data to avoid being blocked from entering new vehicles.",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
//                )
//                Spacer(modifier = Modifier.height(12.dp))
//                LinearProgressIndicator(
//                    progress = { unsyncedCount / 1000f },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(6.dp)
//                        .clip(CircleShape),
//                    color = MaterialTheme.colorScheme.error,
//                    trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
//                )
//            }
//        }
//    }
//}

 //// Here test ui
//@Composable
//fun SyncWarningCard(unsyncedCount: Int) {
// AnimatedVisibility(
////        visible = unsyncedCount >= 900,
//        visible = unsyncedCount <= 900,
//        enter = expandVertically() + fadeIn(),
//        exit = shrinkVertically() + fadeOut()
//    ) {
//        Surface(
//            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
//            shape = RoundedCornerShape(20.dp),
//            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 4.dp)
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        imageVector = Icons.Default.SyncProblem,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.error,
//                        modifier = Modifier.size(24.dp)
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Text(
//                        text = "Storage Limit",
//                        style = MaterialTheme.typography.titleMedium.copy(
//                            fontWeight = FontWeight.ExtraBold,
//                            color = MaterialTheme.colorScheme.onErrorContainer
//                        )
//                    )
//                    Spacer(modifier = Modifier.weight(1f))
//                    Text(
//                        text = "$unsyncedCount/1000",
//                        style = MaterialTheme.typography.titleMedium.copy(
//                            fontWeight = FontWeight.ExtraBold,
//                            color = MaterialTheme.colorScheme.onErrorContainer
//                        )
//                    )
//                }
////                Spacer(modifier = Modifier.height(8.dp))
////                Text(
////                    text = "You have $unsyncedCount/1000 unsynced tickets. Please sync data to avoid being blocked from entering new vehicles.",
////                    style = MaterialTheme.typography.bodySmall,
////                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
////                )
//                Spacer(modifier = Modifier.height(12.dp))
//                LinearProgressIndicator(
//                    progress = { unsyncedCount / 1000f },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(6.dp)
//                        .clip(CircleShape),
//                    color = MaterialTheme.colorScheme.error,
//                    trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
//                )
//            }
//        }
//    }
//}


@Composable
fun SyncWarningCard(unsyncedCount: Int) {

    var expanded by rememberSaveable { mutableStateOf(false) }

    AnimatedVisibility(
        visible = unsyncedCount >= 900,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            onClick = {
                expanded = !expanded
            },
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.SyncProblem,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Storage Limit",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "$unsyncedCount/1000",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (expanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                AnimatedVisibility(visible = expanded) {

                    Column {

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "You have $unsyncedCount/1000 unsynced tickets. Please sync data to avoid being blocked from entering new vehicles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { unsyncedCount / 1000f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
            }
        }
    }
}




