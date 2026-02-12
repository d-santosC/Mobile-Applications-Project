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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.firebase.auth.FirebaseAuth
import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.ui.Alignment
import com.example.mywins.Screen
import com.example.mywins.database.AppDatabase
import com.example.mywins.database.activityEvent.ActivityEvent
import com.example.mywins.database.goal.Goal
import com.example.mywins.database.goal.GoalRepository

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { GoalRepository(context) }
    val scope = rememberCoroutineScope()

    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    val today = LocalDate.now().format(formatter)

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    val categories = listOf("Health", "Hobbies", "Exercise", "Travel", "Work")
    var expanded by remember { mutableStateOf(false) }

    var frequency by remember { mutableStateOf("") }
    val frequencies = listOf("Daily", "Weekly", "Monthly", "Custom")
    var customDate by remember { mutableStateOf("") }

    var isPublic by remember { mutableStateOf(false) }
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""


    //variáveis para o DatePicker
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
        customDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        frequency = "Custom: $customDate"
    }, year, month, day)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 60.dp)
    ) {
        Text("← Back",
            modifier = Modifier.clickable { navController.popBackStack() },
            fontSize = 16.sp,
            color = Color(0xFF7C3AED)
        )

        Spacer(Modifier.height(16.dp))

        Text("New Goal", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(24.dp))

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
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (frequency.startsWith(freq)) Color(0xFF9F7AEA) else Color(0xFFEDEDED),
                        contentColor = if (frequency.startsWith(freq)) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(freq, fontSize = 10.sp)
                }
            }
        }

        if (customDate.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Selected date: $customDate", fontSize = 14.sp, color = Color.Gray)
        }

        Spacer(Modifier.height(16.dp))

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
                val newGoal = Goal(
                    name = name,
                    category = category,
                    frequency = frequency,
                    isPublic = isPublic,
                    userEmail = userEmail,
                    customDate = if (frequency.startsWith("Custom")) customDate else null,
                    createdDate = today
                )

                scope.launch {
                    repository.insertGoal(newGoal)
                    repository.insertActivityEventIfPublic(newGoal, "created")
                    navController.navigate(Screen.Main.route)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F7AEA))
        ) {
            Text("Add Goal", color = Color.White)
        }
    }
}
