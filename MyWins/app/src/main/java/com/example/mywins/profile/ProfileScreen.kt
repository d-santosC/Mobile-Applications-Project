package com.example.mywins.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import java.io.File
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import com.example.mywins.BottomNavBar
import com.example.mywins.Screen
import com.example.mywins.ui.theme.Teal
import com.example.mywins.database.user.User
import com.example.mywins.database.user.UserRepository
import com.example.mywins.database.AppDatabase
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUserEmail = auth.currentUser?.email ?: ""
    val userRepository = remember { UserRepository(context) }
    val db = AppDatabase.getInstance(context)

    var user by remember { mutableStateOf<User?>(null) }
    var showFriendsPopup by remember { mutableStateOf(false) }
    var friends by remember { mutableStateOf(listOf<String>()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUserEmail) {
        user = userRepository.getUser(currentUserEmail)
        friends = db.friendshipsDao().getFriendsOfUser(currentUserEmail)
    }

    Scaffold(bottomBar = { BottomNavBar(navController) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("My Profile", fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            val profilePictureFile = remember(user?.profilePictureUri) {
                user?.profilePictureUri?.let { path ->
                    val file = File(path)
                    if (file.exists()) file else null
                }
            }

            if (profilePictureFile != null) {
                Image(
                    painter = rememberAsyncImagePainter(profilePictureFile),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(user?.name ?: "Name isn't available", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { showFriendsPopup = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F7AEA))
            ) {
                Text("See Friends List", fontSize = 25.sp)
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate("edit_profile") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text("Edit Profile", fontSize = 25.sp)
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEB5757))
            ) {
                Text("Logout", fontSize = 25.sp)
            }
        }

        if (showFriendsPopup) {
            AlertDialog(
                onDismissRequest = { showFriendsPopup = false },
                title = { Text("My Friends") },
                text = {
                    Column {
                        if (friends.isEmpty()) {
                            Text("You have no friends yet")
                        } else {
                            friends.forEach { friend ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(friend)
                                    OutlinedButton(onClick = {
                                        scope.launch {
                                            db.friendshipsDao().removeFriendship(currentUserEmail, friend)
                                            db.friendshipsDao().removeFriendship(friend, currentUserEmail)
                                            friends = db.friendshipsDao().getFriendsOfUser(currentUserEmail)
                                            if (friends.isEmpty()) showFriendsPopup = false
                                        }
                                    }) {
                                        Text("Remove")
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFriendsPopup = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}