package com.example.mywins

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextDecoration
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import com.example.mywins.database.AppDatabase
import com.example.mywins.database.friendships.FriendRequest
import com.example.mywins.database.goal.Goal
import com.example.mywins.database.goal.GoalRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { GoalRepository(context) }
    val currentBackStackEntry = navController.currentBackStackEntry
    var goals by remember { mutableStateOf<List<Goal>>(emptyList()) }
    val db = AppDatabase.getInstance(context)
    val scope = rememberCoroutineScope()

    var showRequestsPopup by remember { mutableStateOf(false) }
    var friendRequests by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }

    var hasPendingRequests by remember { mutableStateOf(false) }

    val categories = listOf("All", "Health", "Hobbies", "Exercise", "Work", "Travel")
    var selectedCategory by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }

    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    LaunchedEffect(currentBackStackEntry) {
        scope.launch {
            goals = repository.getAllGoalsForUser(userEmail)
            val requests = db.friendRequestDao().getReceivedRequests(userEmail)
            friendRequests = requests
            hasPendingRequests = requests.isNotEmpty()
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController)},
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_goal") },
                containerColor = Color(0xFF9F7AEA),
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "MyWins",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    showRequestsPopup = true
                }) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Friend Requests",
                            tint = Color(0xFF9F7AEA)
                        )

                        if (hasPendingRequests) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Red, CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter by Category", color = Color(0xFF9F7AEA), fontWeight = FontWeight.Medium) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6E50A8),
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color(0xFFF2F2F2),
                        unfocusedContainerColor = Color(0xFFF2F2F2)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(50)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            },
                            modifier = Modifier.background(Color.White)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Active Goals", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))

            val activeGoals = goals.filter {
                !it.done && (selectedCategory == "All" || it.category == selectedCategory)
            }

            val doneGoals = goals.filter {
                it.done && (selectedCategory == "All" || it.category == selectedCategory)
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                activeGoals.forEach { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { navController.navigate("edit_goal/${goal.id}") },
                        onToggleDone = { done ->
                            scope.launch {
                                val updatedGoal = goal.copy(
                                    done = done,
                                    doneDate = if (done) getTodayDate() else null
                                )
                                repository.updateGoal(updatedGoal)
                                if (done) {
                                    repository.insertActivityEventIfPublic(updatedGoal, "completed")
                                }
                                goals = repository.getAllGoalsForUser(userEmail)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("Completed", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                doneGoals.forEach { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { navController.navigate("edit_goal/${goal.id}") },
                        onToggleDone = { done ->
                            scope.launch {
                                val updatedGoal = goal.copy(
                                    done = done,
                                    doneDate = if (done) getTodayDate() else null
                                )
                                repository.updateGoal(updatedGoal)
                                goals = repository.getAllGoalsForUser(userEmail)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showRequestsPopup) {
        FriendRequestPopup(
            requests = friendRequests,
            onAccept = { request ->
                scope.launch {
                    db.friendshipsDao().addFriendship(
                        com.example.mywins.database.friendships.Friendship(request.fromEmail, request.toEmail)
                    )
                    db.friendshipsDao().addFriendship(
                        com.example.mywins.database.friendships.Friendship(request.toEmail, request.fromEmail)
                    )
                    db.friendRequestDao().deleteRequest(request.fromEmail, request.toEmail)
                    friendRequests = db.friendRequestDao().getReceivedRequests(userEmail)
                    hasPendingRequests = friendRequests.isNotEmpty()
                    if (friendRequests.isEmpty()) showRequestsPopup = false
                }
            },
            onReject = { request ->
                scope.launch {
                    db.friendRequestDao().deleteRequest(request.fromEmail, request.toEmail)
                    friendRequests = db.friendRequestDao().getReceivedRequests(userEmail)
                    hasPendingRequests = friendRequests.isNotEmpty()
                    if (friendRequests.isEmpty()) showRequestsPopup = false
                }
            },
            onDismiss = {
                showRequestsPopup = false
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    onToggleDone: (Boolean) -> Unit
) {
    val color = getColorForCategory(goal.category)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = goal.done,
                onCheckedChange = { isChecked ->
                    onToggleDone(isChecked)
                }
            )

            Text(
                goal.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                textDecoration = if (goal.done) TextDecoration.LineThrough else null
            )

            Text(goal.category, fontSize = 14.sp)
        }
    }
}

@Composable
fun FriendRequestPopup(
    requests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Friend Requests") },
        text = {
            Column {
                if (requests.isEmpty()) {
                    Text("No pending requests.")
                } else {
                    requests.forEach { req ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                req.fromEmail,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onAccept(req) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF9F7AEA),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Accept")
                                }
                                OutlinedButton(
                                    onClick = { onReject(req) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

fun getColorForCategory(category: String): Color {
    return when (category) {
        "Health" -> Color(0xFFA8DECE)
        "Hobbies" -> Color(0xFFF4937B)
        "Exercise" -> Color(0xFF91E59A)
        "Work" -> Color(0xFFFBE084)
        "Travel" -> Color(0xFFD1A7F4)
        else -> Color(0xFFFFEB75) //default
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getTodayDate(): String {
    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    return LocalDate.now().format(formatter)
}