package com.si.myfoodordering.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.si.myfoodordering.ui.components.AppHeader
import com.si.myfoodordering.ui.components.FloatingAppBottomBar
import com.si.myfoodordering.ui.components.FloatingBottomNavItem
import com.si.myfoodordering.ui.navigation.Screen
import com.si.myfoodordering.ui.theme.PrimaryOrange
import com.si.myfoodordering.ui.viewmodel.AdminMainViewModel
import com.si.myfoodordering.ui.viewmodel.AuthViewModel
import com.si.myfoodordering.ui.viewmodel.OrderViewModel
import com.si.myfoodordering.ui.viewmodel.PlatViewModel

/**
 * Même barre flottante que le client : Accueil · Commandes · Menu (emplacement panier) · Profil.
 */
@Composable
fun AdminMainScreen(
    rootNavController: NavHostController,
    orderViewModel: OrderViewModel,
    platViewModel: PlatViewModel,
    authViewModel: AuthViewModel = hiltViewModel(),
    adminShellViewModel: AdminMainViewModel = hiltViewModel()
) {
    val selectedTab by adminShellViewModel.selectedTab.collectAsState()
    var isEditingProfile by remember { mutableStateOf(false) }

    val navigateToOrderDetail: (Int) -> Unit = { orderId ->
        rootNavController.navigate(Screen.OrderDetail.createRoute(orderId, admin = true))
    }

    val bottomItems = remember {
        listOf(
            FloatingBottomNavItem("Accueil", Icons.Outlined.Home, Icons.Filled.Home),
            FloatingBottomNavItem("Commandes", Icons.AutoMirrored.Outlined.ReceiptLong, Icons.AutoMirrored.Filled.ReceiptLong),
            FloatingBottomNavItem("Menu", Icons.Outlined.RestaurantMenu, Icons.Filled.RestaurantMenu),
            FloatingBottomNavItem("Profil", Icons.Outlined.Person, Icons.Filled.Person),
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppHeader(
                title = when (selectedTab) {
                    0 -> "Accueil"
                    1 -> "Commandes"
                    2 -> "Menu"
                    else -> "Profil"
                },
                compact = true,
                actions = {
                    if (selectedTab == 2) {
                        IconButton(
                            onClick = { rootNavController.navigate(Screen.EditPlat.createRoute(null)) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = "Ajouter un plat",
                                tint = PrimaryOrange,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    if (selectedTab == 3 && !isEditingProfile) {
                        IconButton(onClick = { isEditingProfile = true }) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Modifier le profil",
                                tint = PrimaryOrange
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> AdminDashboardScreen(
                    orderViewModel = orderViewModel,
                    platViewModel = platViewModel,
                    embeddedInTab = true,
                    onOpenOrderDetail = navigateToOrderDetail
                )
                1 -> AdminOrdersTab(
                    orderViewModel = orderViewModel,
                    onOpenOrderDetail = navigateToOrderDetail
                )
                2 -> AdminPlatsScreen(
                    viewModel = platViewModel,
                    embeddedInTab = true,
                    onNavigateToAdd = {
                        rootNavController.navigate(Screen.EditPlat.createRoute(null))
                    },
                    onNavigateToEdit = { plat ->
                        rootNavController.navigate(Screen.EditPlat.createRoute(plat.id))
                    }
                )
                3 -> AdminProfileTab(
                    authViewModel = authViewModel,
                    isEditing = isEditingProfile,
                    onEditChange = { isEditingProfile = it },
                    onLogout = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onOpenClientApp = {
                        rootNavController.navigate(Screen.Home.route) {
                            popUpTo(Screen.AdminMain.route) { inclusive = true }
                        }
                    }
                )
            }

            FloatingAppBottomBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding(),
                items = bottomItems,
                selectedIndex = selectedTab,
                onSelect = { adminShellViewModel.selectTab(it) },
            )
        }
    }
}
