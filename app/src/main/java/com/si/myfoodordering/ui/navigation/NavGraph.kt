package com.si.myfoodordering.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.si.myfoodordering.ui.screens.admin.AdminMainScreen
import com.si.myfoodordering.ui.screens.admin.EditPlatScreen
import com.si.myfoodordering.ui.screens.auth.LoginScreen
import com.si.myfoodordering.ui.screens.auth.RegisterScreen
import com.si.myfoodordering.ui.screens.splash.SplashScreen
import com.si.myfoodordering.ui.screens.cart.CartScreen
import com.si.myfoodordering.ui.screens.checkout.CheckoutScreen
import com.si.myfoodordering.ui.screens.checkout.OrderSuccessScreen
import com.si.myfoodordering.ui.screens.favoris.FavorisScreen
import com.si.myfoodordering.ui.screens.home.HomeScreen
import com.si.myfoodordering.ui.screens.home.PlatDetailScreen
import com.si.myfoodordering.ui.screens.home.PlatNotFoundScreen
import com.si.myfoodordering.ui.screens.orders.OrderDetailScreen
import com.si.myfoodordering.ui.screens.orders.OrdersScreen
import com.si.myfoodordering.ui.screens.profile.ProfileScreen
import com.si.myfoodordering.ui.components.FloatingAppBottomBar
import com.si.myfoodordering.ui.components.FloatingBottomNavItem
import com.si.myfoodordering.ui.viewmodel.CartViewModel
import com.si.myfoodordering.ui.viewmodel.FavorisViewModel
import com.si.myfoodordering.ui.viewmodel.OrderViewModel
import com.si.myfoodordering.ui.viewmodel.PlatViewModel
import com.si.myfoodordering.ui.util.UiMessageBus

sealed class Screen(val route: String, val title: String? = null, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home", "Accueil", Icons.Outlined.Home, Icons.Filled.Home)
    object Favoris : Screen("favoris", "Favoris", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite)
    object Cart : Screen("cart", "Panier", Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart)
    object Orders : Screen("orders", "Commandes", Icons.AutoMirrored.Outlined.ReceiptLong, Icons.AutoMirrored.Filled.ReceiptLong)
    object Profile : Screen("profile", "Profil", Icons.Outlined.Person, Icons.Filled.Person)
    
    object Checkout : Screen("checkout")
    object OrderSuccess : Screen("order_success")
    object OrderDetail : Screen("order_detail/{orderId}?admin={admin}") {
        fun createRoute(orderId: Int, admin: Boolean = false) =
            if (admin) "order_detail/$orderId?admin=true" else "order_detail/$orderId"
    }
    object AdminMain : Screen("admin_main")
    object EditPlat : Screen("edit_plat/{platId}") {
        fun createRoute(platId: Int?) = "edit_plat/$platId"
    }
    object PlatDetail : Screen("plat_detail/{platId}") {
        fun createRoute(platId: Int) = "plat_detail/$platId"
    }
}

@Composable
fun SetupNavGraph(navController: NavHostController = rememberNavController()) {
    val cartViewModel: CartViewModel = hiltViewModel()
    val orderViewModel: OrderViewModel = hiltViewModel()
    val platViewModel: PlatViewModel = hiltViewModel()
    val favorisViewModel: FavorisViewModel = hiltViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarScreens = listOf(Screen.Home, Screen.Favoris, Screen.Cart, Screen.Orders, Screen.Profile)
    val favoriIds by favorisViewModel.favoriIds.collectAsState()
    val shouldShowBottomBar = currentRoute in bottomBarScreens.map { it.route }

    val toastContext = LocalContext.current
    LaunchedEffect(Unit) {
        UiMessageBus.messages.collect { msg ->
            Toast.makeText(toastContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
                enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500)) }
            ) {
                composable(
                    route = Screen.Splash.route,
                    enterTransition = { fadeIn(animationSpec = tween(300)) },
                    exitTransition = { fadeOut(animationSpec = tween(400)) }
                ) {
                    SplashScreen(
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToAdmin = {
                            navController.navigate(Screen.AdminMain.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onLoginSuccess = { isAdmin ->
                            if (isAdmin) {
                                navController.navigate(Screen.AdminMain.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        onNavigateToLogin = { navController.popBackStack() },
                        onRegisterSuccess = { isAdmin ->
                            if (isAdmin) {
                                navController.navigate(Screen.AdminMain.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = platViewModel,
                        favoriIds = favoriIds,
                        onToggleFavori = { platId -> favorisViewModel.toggleFavori(platId) },
                        onAddToCart = { plat -> cartViewModel.addToCart(plat) },
                        onNavigateToDetail = { plat ->
                            plat.id?.let { id ->
                                navController.navigate(Screen.PlatDetail.createRoute(id))
                            }
                        }
                    )
                }
                composable(Screen.Favoris.route) {
                    FavorisScreen(
                        viewModel = favorisViewModel,
                        favoriIds = favoriIds,
                        onToggleFavori = { platId -> favorisViewModel.toggleFavori(platId) },
                        onNavigateToDetail = { plat ->
                            plat.id?.let { id ->
                                navController.navigate(Screen.PlatDetail.createRoute(id))
                            }
                        },
                        onAddToCart = { plat -> cartViewModel.addToCart(plat) }
                    )
                }
                composable(
                    route = Screen.PlatDetail.route,
                    arguments = listOf(navArgument("platId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val platId = backStackEntry.arguments?.getInt("platId") ?: 0
                    val plats by platViewModel.plats.collectAsState()
                    val isLoading by platViewModel.isLoading.collectAsState()
                    val loadError by platViewModel.loadError.collectAsState()
                    val plat = plats.find { it.id == platId }

                    LaunchedEffect(platId, plats.size, loadError) {
                        if (plat == null && plats.isEmpty() && loadError == null) {
                            platViewModel.loadData()
                        }
                    }

                    when {
                        plat != null -> {
                            PlatDetailScreen(
                                plat = plat,
                                isFavori = plat.id != null && plat.id in favoriIds,
                                onToggleFavori = { plat.id?.let { favorisViewModel.toggleFavori(it) } },
                                onAddToCart = { quantity ->
                                    repeat(quantity) { cartViewModel.addToCart(plat) }
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        plats.isNotEmpty() -> {
                            PlatNotFoundScreen(onBack = { navController.popBackStack() })
                        }
                        isLoading && plats.isEmpty() && loadError == null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        loadError != null -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(loadError ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { platViewModel.loadData() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    )
                                ) {
                                    Text("Réessayer")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { navController.popBackStack() }) {
                                    Text("Retour")
                                }
                            }
                        }
                        else -> {
                            PlatNotFoundScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
                composable(Screen.Cart.route) {
                    val cartItems by cartViewModel.cartItems.collectAsState()
                    
                    CartScreen(
                        cartItems = cartItems,
                        onIncrement = { plat -> cartViewModel.addToCart(plat) },
                        onDecrement = { plat -> cartViewModel.removeFromCart(plat) },
                        onRemove = { plat -> cartViewModel.removeFromCart(plat) },
                        onCheckout = { navController.navigate(Screen.Checkout.route) }
                    )
                }
                composable(Screen.Checkout.route) {
                    val cartItems by cartViewModel.cartItems.collectAsState()
                    val total by cartViewModel.totalAmount.collectAsState(initial = 0.0)
                    
                    CheckoutScreen(
                        cartItems = cartItems,
                        total = total,
                        onSuccess = {
                            cartViewModel.clearCart()
                            navController.navigate(Screen.OrderSuccess.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.OrderSuccess.route) {
                    OrderSuccessScreen(
                        onReturnHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Orders.route) {
                     OrdersScreen(
                        viewModel = orderViewModel,
                        onNavigateToDetail = { orderId -> 
                            navController.navigate(Screen.OrderDetail.createRoute(orderId))
                        }
                     )
                }
                composable(
                    route = Screen.OrderDetail.route,
                    arguments = listOf(
                        navArgument("orderId") { type = NavType.IntType },
                        navArgument("admin") {
                            type = NavType.BoolType
                            defaultValue = false
                        }
                    )
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
                    val isAdminDetail = backStackEntry.arguments?.getBoolean("admin") ?: false
                    OrderDetailScreen(
                        orderId = orderId,
                        isAdmin = isAdminDetail,
                        orderViewModel = orderViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Profile.route) {
                     ProfileScreen(
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToAdmin = { navController.navigate(Screen.AdminMain.route) }
                     )
                }
                composable(Screen.AdminMain.route) {
                    AdminMainScreen(
                        rootNavController = navController,
                        orderViewModel = orderViewModel,
                        platViewModel = platViewModel
                    )
                }
                composable(
                    route = Screen.EditPlat.route,
                    arguments = listOf(navArgument("platId") { type = NavType.StringType; nullable = true })
                ) { backStackEntry ->
                    val platIdStr = backStackEntry.arguments?.getString("platId")
                    val platId = if (platIdStr == "null" || platIdStr == null) null else platIdStr.toInt()
                    EditPlatScreen(
                        platId = platId,
                        viewModel = platViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            if (shouldShowBottomBar) {
                val cartCount by cartViewModel.itemsCount.collectAsState(0)
                val selectedClientIndex = bottomBarScreens.indexOfFirst { it.route == currentRoute }
                    .coerceIn(0, bottomBarScreens.lastIndex)
                val cartBadgeIndex = bottomBarScreens.indexOf(Screen.Cart)

                FloatingAppBottomBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .imePadding(),
                    items = bottomBarScreens.map {
                        FloatingBottomNavItem(
                            title = it.title!!,
                            icon = it.icon!!,
                            selectedIcon = it.selectedIcon!!
                        )
                    },
                    selectedIndex = selectedClientIndex,
                    onSelect = { index ->
                        val screen = bottomBarScreens[index]
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    badgeIndex = cartBadgeIndex,
                    badgeCount = cartCount,
                )
            }
        }
    }
}
