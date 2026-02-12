package com.example.mywins.friends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.mywins.BottomNavBar
import com.example.mywins.database.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.example.mywins.database.user.User
import com.example.mywins.database.activityEvent.ActivityEvent
import com.example.mywins.database.friendships.FriendRequest
import kotlinx.coroutines.launch

@Composable
fun FriendsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getInstance(context)

    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    var searchQuery by remember { mutableStateOf("") }
    var friendEvents by remember { mutableStateOf(emptyList<ActivityEvent>()) }
    var usersMap by remember { mutableStateOf(mapOf<String, User>()) }

    LaunchedEffect(currentUserEmail) {
        scope.launch {
            val friendEmails = db.friendshipsDao().getFriendsOfUser(currentUserEmail)
            val allEmails = friendEmails + currentUserEmail
            val events = db.activityEventDao().getEventsFromFriends(allEmails)
            friendEvents = events

            val users = mutableMapOf<String, User>()
            for (email in allEmails) {
                val user = db.userDao().getUserByEmail(email)
                if (user != null) {
                    users[email] = user
                }
            }
            usersMap = users
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Text(
                "Friend Activity",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Add Friend from Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)

            )

            Button(
                onClick = {
                    scope.launch {
                        val userExists = db.userDao().getUserByEmail(searchQuery) != null
                        if (userExists && searchQuery != currentUserEmail) {
                            db.friendRequestDao().sendRequest(
                                FriendRequest(
                                    fromEmail = currentUserEmail,
                                    toEmail = searchQuery,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            searchQuery = ""

                            android.widget.Toast
                                .makeText(context, "Friend request sent", android.widget.Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            android.widget.Toast
                                .makeText(context, "User not found or invalid", android.widget.Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F7AEA))
            ) {
                Text("Send Friend Request")
            }

            Spacer(Modifier.height(24.dp))

            friendEvents.forEach { event ->
                val user = usersMap[event.userEmail]
                if (user != null) {
                    FriendEventCard(
                        event = event,
                        user = user,
                        currentUserEmail = currentUserEmail,
                        userMap = usersMap,
                        onEventDeleted = { deletedId ->
                            friendEvents = friendEvents.filter { it.id != deletedId }
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}