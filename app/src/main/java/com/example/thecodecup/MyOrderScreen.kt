package com.example.thecodecup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


@Composable
fun MyOrderScreen(
    email: String,
    db: AppDatabase,
) {
    val orders by db.orderDao().getOrdersByEmailFlow(email).collectAsState(initial = emptyList())
    val coffeeList by db.coffeeDao().getAllCoffees().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableStateOf(0) }

    // Build a Map for fast coffeeId lookup
    val coffeeMap = remember(coffeeList) {
        coffeeList.associateBy { it.id }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp)) {

        Text(
            "My Orders",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TabButton(title = "On going", selected = selectedTab == 0) { selectedTab = 0 }
            TabButton(title = "History", selected = selectedTab == 1) { selectedTab = 1 }
        }

        Divider(modifier = Modifier.fillMaxWidth(), color = Color.LightGray)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(orders) { order ->
                val coffee = coffeeMap[order.coffeeId]
                if (coffee != null) {
                    if (selectedTab == 0 && order.status == "ongoing") {
                        OrderItemRow(order, coffee, db) {}
                    }
                    if (selectedTab == 1 && order.status == "history") {
                        OrderItemRow(order, coffee, db) {}
                    }
                }
            }
        }

    }
}

@Composable
fun TabButton(title: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = title,
            color = if (selected) Color.Black else Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun OrderItemRow(
    order: OrderEntity,
    coffee: CoffeeEntity,
    db: AppDatabase,
    onStatusChanged: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                if (order.status == "ongoing") {
                    scope.launch {
                        db.orderDao().updateOrder(order.copy(status = "history"))
                        onStatusChanged()
                    }
                }
            }
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(order.dateTime, fontSize = 12.sp, color = Color.Gray)
            Text(String.format("$%.2f", order.price), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = coffee.imageRes),
                contentDescription = coffee.name,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(coffee.name)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = "Location",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(order.address, fontSize = 12.sp, color = Color.Gray)
        }

        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

