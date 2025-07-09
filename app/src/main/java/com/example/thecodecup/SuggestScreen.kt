package com.example.thecodecup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch


@Composable
fun SuggestScreen(
    email: String,
    db: AppDatabase,
    navController: NavController,
    onBackClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var mostOrdered by remember { mutableStateOf<List<CoffeeEntity>>(emptyList()) }
    var recommended by remember { mutableStateOf<List<CoffeeEntity>>(emptyList()) }
    var neverTried by remember { mutableStateOf<List<CoffeeEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            val coffeeDao = db.coffeeDao()
            val orderDao = db.orderDao()

            // Most Ordered by User
            val userTop = orderDao.getTopUserOrders(email)
                .mapNotNull { coffeeDao.getCoffeeById(it.coffeeId) }
            mostOrdered = userTop

            // Recommended Based on Categories
            val categories = orderDao.getUserOrderedCategories(email).distinct()

            recommended = if (categories.isNotEmpty()) {
                val list = coffeeDao.getRecommendedCoffees(email, categories)
                if (list.isNotEmpty()) list else coffeeDao.getRandomCoffees()
            } else {
                coffeeDao.getRandomCoffees()
            }


            // Never Tried
            neverTried = orderDao.getNeverOrderedByUser(email)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Top Bar
        IconButton(onClick = onBackClick) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                "☕ Suggestions for You",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }


        Spacer(Modifier.height(16.dp))

        CoffeeRow(title = "☕ Your Daily Picks", coffees = mostOrdered, email = email, navController = navController)
        Spacer(Modifier.height(16.dp))

        CoffeeRow(title = "🎯 Recommended for You", coffees = recommended, email = email, navController = navController)
        Spacer(Modifier.height(16.dp))

        CoffeeRow(title = "🌟 Discover Something New", coffees = neverTried, email = email, navController = navController)
    }
}


@Composable
fun CoffeeRow(
    title: String,
    coffees: List<CoffeeEntity>,
    email: String,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF324A59)) // Dark blue background
            .padding(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(coffees) { coffee ->
                Column(
                    modifier = Modifier
                        .width(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(8.dp)
                        .clickable {
                            navController.navigate("order/$email/${coffee.id}")
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = coffee.imageRes),
                        contentDescription = coffee.name,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        coffee.name,
                        color = Color.Black,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
