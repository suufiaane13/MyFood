package com.si.myfoodordering.ui.screens.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.si.myfoodordering.ui.components.BrandedCircleIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSuccessScreen(
    onReturnHome: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = scheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 32.dp,
                color = scheme.surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = onReturnHome,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                    ) {
                        Text("Retour au menu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                "Génial !",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = scheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Votre commande est en route",
                fontSize = 16.sp,
                color = scheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, scheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 40.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BrandedCircleIcon(
                        icon = Icons.Outlined.CheckCircle,
                        circleSize = 132.dp,
                        iconSize = 58.dp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Commande Réussie ! 🎉",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Votre festin est en cours de préparation.\nIl sera bientôt chez vous !",
                        fontSize = 15.sp,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
