package com.example.thecodecup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup

@Composable
fun OrderScreen(
    coffeeId: Int,
    onBackClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    email: String
) {
    val context = LocalContext.current
    val db = remember { DatabaseBuilder.getInstance(context) }
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val coffeeList by db.coffeeDao().getAllCoffees().collectAsState(initial = emptyList())
    val coffeeMap = remember(coffeeList) { coffeeList.associateBy { it.id } }

    // Fetch coffee by ID
    val coffee by produceState<CoffeeEntity?>(initialValue = null, coffeeId) {
        value = db.coffeeDao().getCoffeeById(coffeeId)
    }


    if (coffee == null) {
        Text("Loading...")
        return
    }

    var quantity by remember { mutableStateOf(1) }
    var shotType by remember { mutableStateOf("Single") }
    var drinkType by remember { mutableStateOf("Hot") }
    var size by remember { mutableStateOf("Medium") }
    var iceLevel by remember { mutableStateOf(0) }


    val totalPrice = quantity * coffee!!.price

    val cartItems by db.cartDao().getCartItemsByEmailFlow(email).collectAsState(initial = emptyList())
    val cartCount = cartItems.sumOf { it.quantity }


    var showPlusOne by remember { mutableStateOf(false) }
    LaunchedEffect(showPlusOne) {
        if (showPlusOne) {
            kotlinx.coroutines.delay(800)
            showPlusOne = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Details", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                var showCartPreview by remember { mutableStateOf(false) }

                Box {
                    IconButton(onClick = { showCartPreview = true }) {
                        Image(
                            painter = painterResource(id = R.drawable.cart_button),
                            contentDescription = "Cart",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    if (cartCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color.Red, shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartCount.toString(),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (showCartPreview) {
                        Popup(
                            alignment = Alignment.TopEnd,
                            onDismissRequest = { showCartPreview = false }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFf2f6f9),
                                tonalElevation = 8.dp,
                                modifier = Modifier
                                    .width(300.dp)
                                    .heightIn(max = 300.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Cart Preview", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(8.dp))

                                    if (cartItems.isEmpty()) {
                                        Text("Your cart is empty", color = Color.Gray)
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .heightIn(max = 200.dp) // Limit list height so button stays visible
                                        ) {
                                            items(cartItems) { item ->
                                                val coffee = coffeeMap[item.coffeeId]
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 6.dp)
                                                ) {
                                                    Spacer(Modifier.width(8.dp))
                                                    Column {
                                                        Text(coffee?.name ?: "Unknown", fontSize = 14.sp)
                                                        Text(
                                                            "${item.shotType.lowercase()} | ${item.drinkType.lowercase()} | ${item.size.lowercase()} | ${iceLevelLabel(item.iceLevel)} ice | qty: ${item.quantity}",
                                                            fontSize = 12.sp,
                                                            color = Color.Gray
                                                        )

                                                    }
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                showCartPreview = false
                                                onCartClick()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("View Cart")
                                        }
                                    }
                                }

                            }
                        }
                    }
                }


                if (showPlusOne) {
                    Text(text = "+$quantity", color = Color.Green, modifier = Modifier.padding(start = 8.dp))                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Coffee Image
        Image(
            painter = painterResource(id = coffee!!.imageRes),
            contentDescription = coffee!!.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Coffee Description
        Text(
            text = coffee!!.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Coffee Name & Quantity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(coffee!!.name, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (quantity > 1) quantity-- }) { Text("-") }
                Text("$quantity", Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { quantity++ }) { Text("+") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shot Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Shot")

            Row {
                listOf("Single", "Double").forEach {
                    Button(
                        onClick = { shotType = it },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (shotType == it) Color.DarkGray else Color.LightGray
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(it, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Drink Type Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Select")
            Row {
                IconButton(onClick = { drinkType = "Hot" }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_hot),
                        contentDescription = "Hot",
                        tint = if (drinkType == "Hot") Color.Black else Color.LightGray
                    )
                }
                IconButton(onClick = { drinkType = "Cold" }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cold),
                        contentDescription = "Cold",
                        tint = if (drinkType == "Cold") Color.Black else Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Size Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Size")
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf("Small", "Medium", "Large").forEach { sizeOption ->
                    val iconRes = when (sizeOption) {
                        "Small" -> R.drawable.ic_small
                        "Medium" -> R.drawable.ic_medium
                        else -> R.drawable.ic_large
                    }
                    val iconSize = when (sizeOption) {
                        "Small" -> 24.dp
                        "Medium" -> 32.dp
                        else -> 40.dp
                    }
                    IconButton(
                        onClick = { size = sizeOption },
                        modifier = Modifier.size(iconSize + 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = sizeOption,
                            modifier = Modifier.size(iconSize),
                            tint = if (size == sizeOption) Color.Black else Color.LightGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ice Level Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ice")
            Row(verticalAlignment = Alignment.CenterVertically) {
                (0..2).forEach { level ->
                    val iconRes = when (level) {
                        0 -> R.drawable.ic_ice_low
                        1 -> R.drawable.ic_ice_medium
                        else -> R.drawable.ic_ice_high
                    }
                    val iconSize = when (level) {
                        0 -> 24.dp
                        1 -> 32.dp
                        else -> 40.dp
                    }
                    IconButton(
                        onClick = { iceLevel = level },
                        modifier = Modifier.size(iconSize + 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = "Ice $level",
                            modifier = Modifier.size(iconSize),
                            tint = if (iceLevel == level) Color.Black else Color.LightGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Amount", style = MaterialTheme.typography.bodyLarge)
            Text(String.format("$%.2f", totalPrice), style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    val existingItem = db.cartDao().findMatchingItem(
                        email, coffeeId, shotType, drinkType, size, iceLevel
                    )
                    if (existingItem != null) {
                        db.cartDao().updateQuantity(existingItem.id, quantity)
                    } else {
                        db.cartDao().insertCartItem(
                            CartEntity(
                                userEmail = email,
                                coffeeId = coffeeId,
                                quantity = quantity,
                                shotType = shotType,
                                drinkType = drinkType,
                                size = size,
                                iceLevel = iceLevel,
                                pricePerCup = coffee!!.price
                            )
                        )
                    }
                    showPlusOne = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Add to cart")
        }
    }
}
