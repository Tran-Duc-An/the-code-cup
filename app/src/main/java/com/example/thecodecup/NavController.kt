package com.example.thecodecup

import androidx.annotation.DrawableRes
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Orders : Screen("orders")
    object Redeem : Screen("redeem")
    object Profile : Screen("profile")
}


sealed class FunctionScreen(
    val route: String,
    val title: String,
    @DrawableRes val iconSelected: Int,
    @DrawableRes val iconUnselected: Int
) {
    object Home : FunctionScreen(
        "home", "Home",
        R.drawable.home_active_button,
        R.drawable.home_button
    )

    object Orders : FunctionScreen(
        "orders", "Orders",
        R.drawable.orders_active_button,
        R.drawable.orders_button
    )

    object Redeem : FunctionScreen(
        "redeem", "Redeem",
        R.drawable.redeem_active_button,
        R.drawable.redeem_button
    )
}



@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(FunctionScreen.Home, FunctionScreen.Orders, FunctionScreen.Redeem)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)  // Lift nav bar higher from bottom
    ) {
        BottomNavigation(
            backgroundColor = Color.White.copy(alpha = 0.9f),  // Pure white with slight transparency
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(24.dp))
                .align(Alignment.Center)
                .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.25f))
        )
        {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route

                BottomNavigationItem(
                    icon = {
                        Image(
                            painter = painterResource(
                                id = if (isSelected) screen.iconSelected else screen.iconUnselected
                            ),
                            contentDescription = screen.title,
                            modifier = Modifier.size(24.dp)  // Smaller icon size
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        navController.popBackStack(screen.route, inclusive = false)
                        navController.navigate(screen.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    alwaysShowLabel = false  // Hide text labels for minimalist look
                )
            }
        }
    }
}



@Composable
fun MainScreen(db: AppDatabase, email: String, onProfileClick: (String) -> Unit) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(
                    FunctionScreen.Home.route,
                    FunctionScreen.Redeem.route,
                    FunctionScreen.Orders.route
                )
            ) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = FunctionScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(FunctionScreen.Home.route) {
                HomeScreen(
                    email = email,
                    db = db,
                    previewUser = null,
                    navController = navController,
                    onProfileClick = { onProfileClick(email) }
                )
            }
            composable(FunctionScreen.Orders.route) {
                MyOrderScreen(email = email, db = db)
            }
            composable(FunctionScreen.Redeem.route) {
                RewardScreen(email = email, db = db, navController = navController, previewUser = null, previewOrders = null)
            }

            composable(
                "order/{email}/{coffeeId}",
                arguments = listOf(
                    navArgument("email") { type = NavType.StringType },
                    navArgument("coffeeId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                val coffeeId = backStackEntry.arguments?.getInt("coffeeId") ?: -1

                OrderScreen(
                    email = email,
                    coffeeId = coffeeId,
                    onBackClick = { navController.popBackStack() },
                    onCartClick = { navController.navigate("cart/$email") }
                )
            }

            composable(
                "cart/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                CartScreen(
                    email = email,
                    db = db,
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                "orderSuccess/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                OrderSuccessScreen(onTrackOrder = {
                    navController.navigate(FunctionScreen.Orders.route) {
                        popUpTo(FunctionScreen.Home.route)
                        launchSingleTop = true
                    }
                })
            }

            composable(
                "redeem/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                RedeemScreen(
                    email = email,
                    db = db,
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ✅ Suggest Screen Route
            composable(
                route = "suggest/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                SuggestScreen(email = email, db = db, navController = navController, onBackClick = { navController.popBackStack() })
            }

        }
    }
}

@Composable
fun MyApp(db: AppDatabase) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        scope.launch {
           //Clear existing data and populate with initial data if empty
//            db.orderDao().deleteAllOrders()
//            db.cartDao().deleteAllCartItems()
//            db.userDao().deleteAllUsers()
//            db.redeemDao().deleteAllRedeemItems()
//            db.coffeeDao().deleteAllCoffees()

            val coffeeDao = db.coffeeDao()
            val redeemDao = db.redeemDao()

            val existingCoffees = coffeeDao.getAllCoffees().first()
            val existingRedeems = redeemDao.getAllRedeemItems().first()

            // Insert Coffees only if empty
            if (existingCoffees.isEmpty()) {
                val coffees = listOf(
                    CoffeeEntity(name = "Americano", imageRes = R.drawable.americano, description = "Classic black coffee with rich aroma.", price = 3.00, redeemPoint = 15, category = "Black Coffee"),
                    CoffeeEntity(name = "Cappuccino", imageRes = R.drawable.cappuccino, description = "Espresso with steamed milk foam.", price = 3.50, redeemPoint = 18, category = "Milk-based"),
                    CoffeeEntity(name = "Mocha", imageRes = R.drawable.mocha, description = "Chocolate-flavored coffee with milk.", price = 4.00, redeemPoint = 20, category = "Milk-based"),
                    CoffeeEntity(name = "Flat White", imageRes = R.drawable.flatwhite, description = "Smooth espresso with velvety milk.", price = 3.75, redeemPoint = 17, category = "Milk-based"),
                    CoffeeEntity(name = "Espresso", imageRes = R.drawable.espresso, description = "Strong, rich shot of pure coffee.", price = 2.50, redeemPoint = 10, category = "Black Coffee"),
                    CoffeeEntity(name = "Macchiato", imageRes = R.drawable.machiatto, description = "Espresso with a dollop of steamed milk.", price = 3.20, redeemPoint = 16, category = "Black Coffee"),
                    CoffeeEntity(name = "Latte", imageRes = R.drawable.latte, description = "Creamy espresso with steamed milk.", price = 3.80, redeemPoint = 18, category = "Milk-based"),
                    CoffeeEntity(name = "Irish Coffee", imageRes = R.drawable.irish_coffee, description = "Coffee mixed with Irish whiskey and cream.", price = 5.00, redeemPoint = 25, category = "Specialty")
                )

                coffees.forEach { coffeeDao.insertCoffee(it) }
            }

            // Fetch updated Coffee list after insert (IDs generated)
            val updatedCoffees = coffeeDao.getAllCoffees().first()

            // Insert Redeems only if empty
            if (existingRedeems.isEmpty() && updatedCoffees.isNotEmpty()) {
                val redeemList = listOf(
                    RedeemEntity(coffeeId = updatedCoffees.first { it.name == "Americano" }.id, redeemPoint = 150, availableUntil = "01.07.2025"),
                    RedeemEntity(coffeeId = updatedCoffees.first { it.name == "Cappuccino" }.id, redeemPoint = 180, availableUntil = "015.07.2025"),
                    RedeemEntity(coffeeId = updatedCoffees.first { it.name == "Mocha" }.id, redeemPoint = 200, availableUntil = "20.06.2025"),
                    RedeemEntity(coffeeId = updatedCoffees.first { it.name == "Flat White" }.id, redeemPoint = 170, availableUntil = "20.07.2025"),
                )
                redeemList.forEach { redeemDao.insertRedeem(it) }
            }
        }
    }


    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { email ->
                    navController.navigate("${Screen.Home.route}/$email") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                db = db
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(onSignUpSuccess = { navController.popBackStack() }, db = db, onBackClick = {navController.popBackStack()})
        }
        composable(
            "home/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            MainScreen(db = db, email = email, onProfileClick = { email ->
                navController.navigate("profile/$email")
            })
        }
        composable("profile/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ProfileScreen(
                email = email,
                db = db,
                onBackClick = { navController.popBackStack() },
                onLogOutClick = {
                    navController.navigate("login") {
                        popUpTo("home/{email}") { inclusive = true }
                    }
                }
            )
        }

    }
}
