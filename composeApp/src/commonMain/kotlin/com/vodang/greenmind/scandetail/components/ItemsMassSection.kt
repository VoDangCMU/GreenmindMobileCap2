package com.vodang.greenmind.scandetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.fmt
import androidx.compose.ui.res.stringResource
import com.vodang.greenmind.scandetail.ScanItem
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.scandetail.neutralGray400
import com.vodang.greenmind.scandetail.neutralGray700
import com.vodang.greenmind.scandetail.scanGreen
import com.vodang.greenmind.scandetail.massBlue
import com.vodang.greenmind.scandetail.massBlueBg

// ── Items Section ─────────────────────────────────────────────────────────────

@Composable
fun ItemsSection(
    items: List<ScanItem>?,
    modifier: Modifier = Modifier,
) {
    val s = LocalAppStrings.current
    val hasItems = !items.isNullOrEmpty()
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        // Summary row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasItems) Modifier.clickable { expanded = !expanded } else Modifier)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(s.detectedItemsList, fontSize = 12.sp, color = neutralGray400)
                if (!hasItems) {
                    Text(s.notApplicable, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = neutralGray400)
                    Text(s.noDataYet, fontSize = 11.sp, color = neutralGray400)
                } else {
                    val totalQty = items.sumOf { it.quantity }
                    Text("$totalQty", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = scanGreen)
                    Text(s.massItemsDetected(totalQty), fontSize = 11.sp, color = neutralGray400)
                }
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (hasItems) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (hasItems) Icons.Filled.Inbox else Icons.Filled.Help,
                    contentDescription = null,
                    tint = if (hasItems) scanGreen else neutralGray400,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Expanded detail
        if (expanded && hasItems) {
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(scanGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "${items.indexOf(item) + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scanGreen,
                                )
                            }
                            Text(item.name, fontSize = 12.sp, color = neutralGray700)
                        }
                        Text(
                            item.massKg?.let { "%.2f kg".fmt(it) } ?: "×${item.quantity}",
                            fontSize = 12.sp,
                            color = neutralGray400,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (item != items.last()) {
                        HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// ── Mass Section ──────────────────────────────────────────────────────────────

@Composable
fun MassSection(
    totalMassKg: Double?,
    itemsMass: List<com.vodang.greenmind.scandetail.ScanItemMass>?,
    modifier: Modifier = Modifier,
) {
    val s = LocalAppStrings.current
    val hasMass = totalMassKg != null || !itemsMass.isNullOrEmpty()
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        // Summary row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasMass) Modifier.clickable { expanded = !expanded } else Modifier)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(s.totalMassLabel, fontSize = 12.sp, color = neutralGray400)
                if (totalMassKg == null) {
                    Text(s.notApplicable, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = neutralGray400)
                    Text(s.noMassData, fontSize = 11.sp, color = neutralGray400)
                } else {
                    Text(s.massKg(totalMassKg), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = massBlue)
                    Text(s.itemMassesLabel, fontSize = 11.sp, color = neutralGray400)
                }
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (totalMassKg != null) massBlueBg else Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Category,
                    contentDescription = null,
                    tint = if (totalMassKg != null) massBlue else neutralGray400,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Expanded detail
        if (expanded && hasMass) {
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (totalMassKg != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(s.totalMassLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = neutralGray700)
                        Text(s.massKg(totalMassKg), fontSize = 12.sp, color = massBlue, fontWeight = FontWeight.Bold)
                    }
                }

                if (!itemsMass.isNullOrEmpty()) {
                    if (totalMassKg != null) {
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(s.itemMassesLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = neutralGray700)
                    itemsMass.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(s.itemBullet(item.name), fontSize = 12.sp, color = neutralGray700, modifier = Modifier.weight(1f))
                            Text(s.massKgPrecision3(item.massKg), fontSize = 12.sp, color = neutralGray400, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}