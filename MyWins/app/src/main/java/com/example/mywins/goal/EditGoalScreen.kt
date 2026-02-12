package com.example.mywins.goal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.app.DatePickerDialog
import androidx.compose.material3.Checkbox
import com.example.mywins.Screen
import com.example.mywins.database.goal.Goal
import com.example.mywins.database.goal.GoalRepository
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalScreen(navController: NavController, goal: Goal?) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = remember { GoalRepository(context) }
    var expanded by remember { mutableStateOf(false) }

    if (goal == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Loading Goal...")
        }
        return
    }

    var name by remember(goal) { mutableStateOf(goal.name) }
    var category by remember(goal) { mutableStateOf(goal.category) }
    var frequency by remember(goal) { mutableStateOf(goal.frequency) }
    var customDate by remember(goal) { mutableStateOf(goal.customDate ?: "") }
    var isPublic by remember(goal) { mutableStateOf(goal.isPublic) }

    val categories = listOf("Health", "Hobbies", "Exercise", "Travel", "Work")
    val frequencies = listOf("Daily", "Weekly", "Monthly", "Custom")

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(context, { _, y, m, d ->
        customDate = "$d/${m + 1}/$y"
        frequency = "Custom: $customDate"
    }, year, month, day)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 60.dp)
    ) {
        Text("← Back", modifier = Modifier.clickable { navController.popBackStack() },
            fontSize = 16.sp, color = Color(0xFF7C3AED))

        Spacer(Modifier.height(16.dp))


        Text("Edit Goal", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Goal Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Frequency", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            frequencies.forEach { freq ->
                Button(
                    onClick = {
                        if (freq == "Custom") {
                            datePickerDialog.show()
                        } else {
                            frequency = freq
                            customDate = ""
                        }
                    },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (frequency.startsWith(freq)) Color(0xFF9F7AEA) else Color(0xFFEDEDED),
                        contentColor = if (frequency.startsWith(freq)) Color.White else Color.Black
                    )
                ) {
                    Text(freq, fontSize = 10.sp)
                }
            }
        }

        if (customDate.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Selected date: $customDate", fontSize = 14.sp, color = Color.Gray)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isPublic,
                onCheckedChange = { isPublic = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Make goal public")
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    val updatedGoal = goal.copy(
                        name = name,
                        category = category,
                        frequency = frequency,
                        isPublic = isPublic,
                        customDate = if (frequency.startsWith("Custom")) customDate else null
                    )
                    repository.updateGoal(updatedGoal)
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F7AEA))
        ) {
            Text("Edit Goal", color = Color.White)
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    repository.deleteGoal(goal)
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Delete Goal", color = Color.White)
        }
    }
}
