package com.example.thecodecup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset

@Composable
fun HomeScreen(
    email: String = "",
    db: AppDatabase? = null,
    previewUser: UserEntity? = null,
    onProfileClick: () -> Unit = {},
    navController: NavController,
    onCartClick: () -> Unit = {}
) {
    var user by remember { mutableStateOf(previewUser) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(email) {
        if (db != null && previewUser == null) {
            scope.launch {
                user = db.userDao().getUserByEmail(email)
            }
        }
    }

    val context = LocalContext.current
    val database = remember { DatabaseBuilder.getInstance(context) }

    val cartItems by database.cartDao().getCartItemsByEmailFlow(email).collectAsState(initial = emptyList())
    val coffeeList by database.coffeeDao().getAllCoffees().collectAsState(initial = emptyList())

    val cartCount = cartItems.sumOf { it.quantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Good morning", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                user?.let {
                    Text(it.fullName, color = Color.Black, style = MaterialTheme.typography.titleLarge)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(onClick = { navController.navigate("cart/$email") }, modifier = Modifier.size(48.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.cart_button),
                            contentDescription = "Cart Button",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (cartCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .background(Color.Red, shape = RoundedCornerShape(50))
                                .padding(4.dp)
                        ) {
                            Text(cartCount.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }


                }
                Spacer(modifier = Modifier.width(16.dp))

                // Suggest Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { navController.navigate("suggest/$email") }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.suggest_button),
                        contentDescription = "Suggestions",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }


                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = { onProfileClick() }, modifier = Modifier.size(48.dp)) {
                    Image(painter = painterResource(id = R.drawable.profile_button), contentDescription = "Profile Button", modifier = Modifier.fillMaxSize())
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        user?.let {
            LoyaltyCard(currentStamps = it.loyaltyStamps, maxStamps = 8)
        }

        Spacer(modifier = Modifier.height(24.dp))

        CoffeeGrid(
            coffeeList = coffeeList,
            onCoffeeClick = { coffee ->
                navController.navigate("order/$email/${coffee.id}")
            }
        )
    }
}


@Composable
fun LoyaltyCard(
    currentStamps: Int,
    maxStamps: Int = 8
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(color = Color(0xFF2C3E50), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Loyalty card",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$currentStamps / $maxStamps",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..maxStamps) {
                Image(
                    painter = painterResource(
                        id = if (i <= currentStamps) R.drawable.cup_filled else R.drawable.cup_empty
                    ),
                    contentDescription = "Stamp $i",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun CoffeeGrid(
    coffeeList: List<CoffeeEntity>,
    onCoffeeClick: (CoffeeEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(color = Color(0xFF2C3E50), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Choose your coffee", color = Color.White, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(coffeeList) { coffee ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(8.dp)
                        .clickable { onCoffeeClick(coffee) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(painter = painterResource(id = coffee.imageRes), contentDescription = coffee.name, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(coffee.name, color = Color.Black, fontSize = 16.sp)
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val dummyNavController = rememberNavController()

    HomeScreen(
        previewUser = UserEntity(
            email = "ducan@example.com",
            fullName = "Duc An",
            phoneNumber = "0123456789",
            password = "123456",
            loyaltyStamps = 4,
            address = "123 Street, City"
        ),
        navController = dummyNavController
    )
}

