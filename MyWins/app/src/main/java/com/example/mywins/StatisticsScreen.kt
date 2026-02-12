package com.example.mywins

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.mywins.database.goal.Goal
import com.example.mywins.database.goal.GoalRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { GoalRepository(context) }
    val auth = FirebaseAuth.getInstance()
    val currentUserEmail = auth.currentUser?.email ?: ""


    var goals by remember { mutableStateOf(emptyList<Goal>()) }
    var selectedPeriod by remember { mutableStateOf("Week") }

    LaunchedEffect(currentUserEmail) {
        Log.d("Stats", "A carregar goals para $currentUserEmail")
        try {
            goals = repository.getAllGoalsForUser(currentUserEmail)
            Log.d("Stats", "Carregou ${goals.size} goals")
        } catch (e: Exception) {
            Log.e("Stats", "Erro ao carregar goals", e)
        }
    }

    val now = LocalDate.now()
    val start = when (selectedPeriod) {
        "Week" -> now.with(DayOfWeek.MONDAY)
        "Month" -> now.withDayOfMonth(1)
        "Year" -> now.withDayOfYear(1)
        else -> now.with(DayOfWeek.MONDAY)
    }


    var doneGoals by remember { mutableStateOf(emptyList<Goal>()) }
    var pendingGoals by remember { mutableStateOf(emptyList<Goal>()) }
    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")

    val doneInPeriod = doneGoals.filter { it.isInPeriod(start, now) }
    val pendingInPeriod = pendingGoals.filter {
        try {
            val created = LocalDate.parse(it.createdDate, formatter)
            created in start..now
        } catch (e: Exception) {
            false
        }
    }

    val total = doneInPeriod.size + pendingInPeriod.size
    val percent = if (total > 0) (doneInPeriod.size.toFloat() / total * 100).toInt() else 0

    LaunchedEffect(currentUserEmail) {
        try {
            doneGoals = repository.getDoneGoalsForUser(currentUserEmail)
            pendingGoals = repository.getPendingGoalsForUser(currentUserEmail)
        } catch (e: Exception) {
            Log.e("Stats", "Erro ao carregar goals do utilizador", e)
        }
    }

    val categoryDistribution = doneInPeriod.groupingBy { it.category }
        .eachCount()
        .mapValues { (_, count) -> count.toFloat() / doneInPeriod.size }

    val streak = computeStreak(goals)

    Scaffold(bottomBar = { BottomNavBar(navController) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Statistics", fontSize = 40.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(30.dp))

            Text("$percent%", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Week", "Month", "Year").forEach { period ->
                    Button(
                        onClick = { selectedPeriod = period },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == period) Color(0xFF9F7AEA) else Color.LightGray,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Text(period)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Categorias", fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                categoryDistribution.forEach { (category, proportion) ->
                    if (proportion > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(proportion.coerceAtLeast(0.01f))
                                .height(20.dp)
                                .padding(end = 2.dp)
                                .background(categoryColor(category))
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                categoryDistribution.forEach { (category, _) ->
                    Text(category, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Streak", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("$streak days", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Goal.wasDoneInPeriod(start: LocalDate, end: LocalDate): Boolean {
    if (!done || doneDate.isNullOrBlank()) return false
    return try {
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        val date = LocalDate.parse(doneDate, formatter)
        date in start..end
    } catch (e: Exception) {
        Log.e("GoalDateParse", "Erro ao fazer parse do doneDate: $doneDate", e)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun computeStreak(goals: List<Goal>): Int {
    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    val today = LocalDate.now()
    var streak = 0
    var current = today

    while (true) {
        val currentDateStr = current.format(formatter)
        val anyDoneThatDay = goals.any { it.done && it.doneDate == currentDateStr }

        if (anyDoneThatDay) {
            streak++
            current = current.minusDays(1)
        } else {
            break
        }
    }

    return streak
}

fun categoryColor(category: String): Color {
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
fun Goal.isInPeriod(start: LocalDate, end: LocalDate): Boolean {
    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")

    val custom = customDate?.let {
        try {
            LocalDate.parse(it, formatter)
        } catch (e: Exception) {
            null
        }
    }

    val done = doneDate?.let {
        try {
            LocalDate.parse(it, formatter)
        } catch (e: Exception) {
            null
        }
    }

    return (custom != null && custom in start..end) || (done != null && done in start..end)
}