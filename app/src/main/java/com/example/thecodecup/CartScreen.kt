package com.example.thecodecup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.NavController

@Composable
fun CartScreen(
    email: String,
    db: AppDatabase,
    navController: NavController,
    onBackClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val cartItems by db.cartDao().getCartItemsByEmailFlow(email).collectAsState(initial = emptyList())
    val coffeeList by db.coffeeDao().getAllCoffees().collectAsState(initial = emptyList())

    val coffeeMap = coffeeList.associateBy { it.id }

    val totalPrice = cartItems.sumOf {
        val coffee = coffeeMap[it.coffeeId]
        (coffee?.price ?: 0.0) * it.quantity
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "My Cart",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn {
                items(cartItems) { item ->
                    CartItemRow(item = item, coffee = coffeeMap[item.coffeeId], db = db, scope = scope)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Price", color = Color.Gray)
                Text(String.format("$%.2f", totalPrice), style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = {
                    scope.launch {
                        val user = db.userDao().getUserByEmail(email) ?: return@launch
                        val address = user.address
                        val cartSnapshot = db.cartDao().getCartItemsByEmailFlow(email).first()

                        if (cartSnapshot.isEmpty()) return@launch

                        var totalPoints = 0
                        val totalStamps = cartSnapshot.sumOf { it.quantity }

                        cartSnapshot.forEach { item ->
                            val coffee = coffeeMap[item.coffeeId] ?: return@forEach

                            db.orderDao().insertOrder(
                                OrderEntity(
                                    userEmail = email,
                                    coffeeId = coffee.id,
                                    address = address,
                                    price = item.quantity * coffee.price,
                                    quantity = item.quantity,
                                    dateTime = getCurrentDateTimeString()
                                )
                            )

                            totalPoints += coffee.redeemPoint * item.quantity
                        }

                        // Calculate new stamp value with wrap-around
                        var newStamps = 0
                        if(user.loyaltyStamps + totalStamps >= 8) {
                            newStamps = 8
                        } else {
                            newStamps = user.loyaltyStamps + totalStamps
                        }

                        db.userDao().updateStampsAndPoints(
                            email,
                            newStamps,
                            user.loyaltyPoint + totalPoints
                        )

                        db.cartDao().clearCartByEmail(email)
                        navController.navigate("orderSuccess/$email")
                    }
                }
            )
            {
                Icon(painter = painterResource(id = R.drawable.cart_button), contentDescription = "Cart", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Checkout", color = Color.White)
            }
        }
    }
}


fun getCurrentDateTimeString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

@Composable
fun CartItemRow(
    item: CartEntity,
    coffee: CoffeeEntity?,
    db: AppDatabase,
    scope: CoroutineScope
) {
    if (coffee == null) return

    var offsetX by remember { mutableStateOf(0f) }
    val maxOffset = 150f

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .background(Color(0xFFF8FAFD), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    offsetX = (offsetX + dragAmount).coerceIn(-maxOffset, 0f)
                }
            }
    ) {
        if (offsetX <= -maxOffset * 0.7f) {
            IconButton(
                onClick = {
                    scope.launch {
                        db.cartDao().deleteCartItemById(item.id)
                        offsetX = 0f
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
                    .background(Color(0xFFFFE5E5), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_delete), contentDescription = "Delete", tint = Color.Red)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset(x = offsetX.dp).fillMaxWidth().padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = coffee.imageRes),
                contentDescription = coffee.name,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(coffee.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${item.shotType.lowercase()} | ${item.drinkType.lowercase()} | ${item.size.lowercase()} | ${iceLevelLabel(item.iceLevel)} ice",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text("x ${item.quantity}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


