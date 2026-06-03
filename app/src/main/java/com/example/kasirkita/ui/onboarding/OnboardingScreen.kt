package com.example.kasirkita.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasirkita.ui.theme.GoldDark
import com.example.kasirkita.ui.theme.GoldPrimary
import com.example.kasirkita.ui.theme.TextPrimary
import com.example.kasirkita.ui.theme.TextSecondary

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Selamat Datang di KasirKita",
            description = "Kelola kasir, pelanggan, dan pengeluaran bisnis Anda dengan mudah",
            icon = Icons.Default.CheckCircle
        ),
        OnboardingPage(
            title = "Manajemen Kas Modern",
            description = "Catat setiap transaksi kas dengan real-time dan laporan akurat",
            icon = Icons.Default.CheckCircle
        ),
        OnboardingPage(
            title = "Pantau Pelanggan",
            description = "Kelola data pelanggan dan lacak riwayat transaksi mereka",
            icon = Icons.Default.CheckCircle
        ),
        OnboardingPage(
            title = "Kontrol Pengeluaran",
            description = "Monitor pengeluaran bisnis dengan dashboard analytics yang detail",
            icon = Icons.Default.CheckCircle
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            OnboardingPageContent(pages[pageIndex])
        }

        // Indicator dots
        Row(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) GoldPrimary else GoldPrimary.copy(alpha = 0.3f)
                        )
                )
                if (index < pages.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // Navigation buttons
        AnimatedVisibility(
            visible = pagerState.currentPage < pages.size - 1,
            enter = slideInVertically() + fadeIn()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigateToLogin() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GoldPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            GoldPrimary
                        ).brush
                    )
                ) {
                    Text("Skip")
                }

                Button(
                    onClick = { /* Ke page berikutnya via pagerState */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary
                    )
                ) {
                    Text("Selanjutnya", color = Color.White)
                }
            }
        }

        AnimatedVisibility(
            visible = pagerState.currentPage == pages.size - 1,
            enter = slideInVertically() + fadeIn()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onNavigateToLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary
                    )
                ) {
                    Text("Mulai Sekarang", color = Color.White, fontSize = 16.sp)
                }

                TextButton(
                    onClick = { onNavigateToLogin() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login dengan akun lama", color = GoldDark)
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = GoldPrimary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
