package com.example.thecodecup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun RewardScreen(
    email: String = "",
    db: AppDatabase? = null,
    navController: NavController,
    previewUser: UserEntity? = null,
    previewOrders: List<Pair<OrderEntity, CoffeeEntity>>? = null

) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf(previewUser) }
    val orders by db!!.orderDao().getOrdersByEmailFlow(email).collectAsState(initial = emptyList())
    val coffees by db.coffeeDao().getAllCoffees().collectAsState(initial = emptyList())

    val ordersWithCoffee = orders.mapNotNull { order ->
        val coffee = coffees.find { it.id == order.coffeeId }
        if (coffee != null) order to coffee else null
    }

    LaunchedEffect(email) {
        if (previewUser == null) {
            scope.launch {
                user = db?.userDao()?.getUserByEmail(email)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {

        Text(
            text = "Rewards",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Loyalty Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = (user?.loyaltyStamps ?: 0) >= 8) {
                    scope.launch {
                        db?.userDao()?.updateUser(
                            user!!.copy(loyaltyStamps = 0)
                        )
                        user = db?.userDao()?.getUserByEmail(email)  // Refresh user data
                    }
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF324A59)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Loyalty Card", fontSize = 16.sp, color = Color.White)
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val stamps = user?.loyaltyStamps ?: 0
                        repeat(8) { i ->
                            Image(
                                painter = painterResource(
                                    id = if (i < stamps) R.drawable.cup_filled else R.drawable.cup_empty
                                ),
                                contentDescription = "Stamp",
                                modifier = Modifier.size(32.dp).padding(4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("${user?.loyaltyStamps ?: 0} / 8", color = Color.White)
                if ((user?.loyaltyStamps ?: 0) >= 8) {
                    Text("Tap to reset stamps", color = Color.Yellow, fontSize = 12.sp)
                }
            }
        }


        Spacer(Modifier.height(16.dp))

        // Points
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF324A59)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("My Points:", color = Color.White)
                    Text("${user?.loyaltyPoint ?: 0}", color = Color.White, fontSize = 28.sp)
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { navController.navigate("redeem/$email") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA2CDE9))
                ) {
                    Text("Redeem drinks")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("History Rewards", style = MaterialTheme.typography.titleMedium)

        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(ordersWithCoffee) { (order, coffee) ->
                HistoryRewardItem(order = order, coffee = coffee)
            }
        }
    }
}

@Composable
fun HistoryRewardItem(order: OrderEntity, coffee: CoffeeEntity) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(coffee.name, style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(order.dateTime, fontSize = 12.sp, color = Color.Gray)
            Text("+ ${coffee.redeemPoint*order.quantity} Pts", color = Color.Black)
        }
        Divider(Modifier.padding(top = 8.dp))
    }
}



