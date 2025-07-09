package com.example.thecodecup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    email: String,
    db: AppDatabase,
    onBackClick: () -> Unit = {},
    onLogOutClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserEntity?>(null) }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var isEditingFullName by remember { mutableStateOf(false) }
    var isEditingPhone by remember { mutableStateOf(false) }
    var isEditingAddress by remember { mutableStateOf(false) }

    LaunchedEffect(email) {
        scope.launch {
            val loadedUser = db.userDao().getUserByEmail(email)
            if (loadedUser != null) {
                user = loadedUser
                fullName = loadedUser.fullName
                phoneNumber = loadedUser.phoneNumber
                address = loadedUser.address
            }
        }
    }

    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(onClick = { onBackClick() }, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Full Name Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            if (it.length <= 30) fullName = it
                        },
                        label = { Text("Full Name") },
                        modifier = Modifier.weight(1f),
                        enabled = isEditingFullName
                    )
                    IconButton(onClick = { isEditingFullName = !isEditingFullName }) {
                        Icon(
                            painter = painterResource(id = if (isEditingFullName) R.drawable.ic_done else R.drawable.ic_edit),
                            contentDescription = "Edit Full Name"
                        )
                    }
                }

                // Phone Number Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.weight(1f),
                        enabled = isEditingPhone
                    )
                    IconButton(onClick = { isEditingPhone = !isEditingPhone }) {
                        Icon(
                            painter = painterResource(id = if (isEditingPhone) R.drawable.ic_done else R.drawable.ic_edit),
                            contentDescription = "Edit Phone"
                        )
                    }
                }

                // Email (read-only)
                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )

                // Address Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            if (it.length <= 100) address = it
                        },
                        label = { Text("Address") },
                        modifier = Modifier.weight(1f),
                        enabled = isEditingAddress
                    )
                    IconButton(onClick = { isEditingAddress = !isEditingAddress }) {
                        Icon(
                            painter = painterResource(id = if (isEditingAddress) R.drawable.ic_done else R.drawable.ic_edit),
                            contentDescription = "Edit Address"
                        )
                    }
                }

                Button(
                    onClick = {
                        if (fullName.isNotBlank() && phoneNumber.isNotBlank() && address.isNotBlank()) {
                            scope.launch {
                                db.userDao().updateUser(
                                    user!!.copy(
                                        fullName = fullName,
                                        phoneNumber = phoneNumber,
                                        address = address
                                    )
                                )
                                successMessage = "Update successful"
                                errorMessage = ""
                                isEditingFullName = false
                                isEditingPhone = false
                                isEditingAddress = false
                            }
                        } else {
                            errorMessage = "Please fill all required fields."
                            successMessage = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text("Save Changes")
                }

                Button(
                    onClick = { onLogOutClick() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
                ) {
                    Text("Log Out", color = Color.White)
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            } else if (successMessage.isNotEmpty()) {
                Text(text = successMessage, color = Color.Green, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}


