package com.example.thecodecup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*



@Composable
fun RedeemScreen(
    email: String,
    db: AppDatabase,
    navController: NavController,
    onBackClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(email) {
        user = db.userDao().getUserByEmail(email)
    }

    // Redeem list from RedeemEntity JOIN CoffeeEntity
    val redeems by db.redeemDao().getAllRedeemItems().collectAsState(initial = emptyList())
    val coffees by db.coffeeDao().getAllCoffees().collectAsState(initial = emptyList())

// Map Redeem with Coffee details
    val redeemWithCoffee = redeems.mapNotNull { redeem ->
        val coffee = coffees.find { it.id == redeem.coffeeId }
        coffee?.let { coffeeEntity -> redeem to coffeeEntity }
    }


    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {

        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Adjust height if needed
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Redeem Rewards",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Redeemable Items List
        LazyColumn {
            items(redeemWithCoffee) { (redeem, coffee) ->
                RedeemItemRow(
                    coffee = coffee,
                    redeem = redeem,
                    userPoints = user?.loyaltyPoint ?: 0,
                    email = email,
                    db = db,
                    navController = navController,
                    onRedeemed = {
                        scope.launch {
                            user = db.userDao().getUserByEmail(email)
                        }
                    }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}


@Composable
fun RedeemItemRow(
    coffee: CoffeeEntity,
    redeem: RedeemEntity,
    userPoints: Int,
    email: String,
    db: AppDatabase,
    navController: NavController,
    onRedeemed: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    // Date format must match how you stored it
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    val availableDate = redeem.availableUntil?.let {
        runCatching { dateFormat.parse(it) }.getOrNull()
    }

    val today = Date()
    val isAvailable = availableDate != null && today.before(availableDate)
    val canRedeem = userPoints >= redeem.redeemPoint && isAvailable

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = coffee.imageRes),
                contentDescription = coffee.name,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(coffee.name, style = MaterialTheme.typography.bodyLarge)
                Text("Available until ${redeem.availableUntil}", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Button(
            onClick = {
                scope.launch {
                    if (canRedeem) {
                        db.userDao().decreaseLoyaltyPoints(email, redeem.redeemPoint)

                        val user = db.userDao().getUserByEmail(email) ?: return@launch

                        db.orderDao().insertOrder(
                            OrderEntity(
                                userEmail = email,
                                coffeeId = coffee.id,
                                address = user.address,
                                price = 0.0,
                                quantity = 1,
                                dateTime = getCurrentDateTimeString()
                            )
                        )

                        onRedeemed()
                        navController.navigate("orderSuccess/$email")
                    }
                }
            },
            enabled = canRedeem,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF294C60))
        ) {
            Text("${redeem.redeemPoint} pts", color = Color.White)
        }
    }
}
