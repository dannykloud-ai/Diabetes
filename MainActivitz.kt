 package com.example.diabeticsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiabeticWorkoutScreen()
                }
            }
        }
    }
}

@Composable
fun DiabeticWorkoutScreen() {
    var inputGlycemia by remember { mutableStateOf("") }
    var activeInsulin by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<GlycemiaCheckResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Kontrola glykémie pred cvičením", fontSize = 20.sp, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = inputGlycemia,
            onValueChange = { inputGlycemia = it },
            label = { Text("Aktuálna glykémia (mmol/l)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = activeInsulin,
                onCheckedChange = { activeInsulin = it }
            )
            Text(text = "Pôsobí aktívny inzulín?")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val value = inputGlycemia.replace(",", ".").toDoubleOrNull() ?: 0.0
                result = evaluatePreWorkoutGlycemia(value, activeInsulin, 30)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Vyhodnotiť bezpečnosť", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        result?.let { res ->
            val boxColor = if (res.isSafeToExercise) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            val textColor = if (res.isSafeToExercise) Color(0xFF2E7D32) else Color(0xFFC62828)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(boxColor)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = if (res.isSafeToExercise) "MÔŽETE CVIČIŤ" else "CVIČENIE ODLOŽTE",
                        color = textColor,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = res.warningMessage, color = textColor)
                    if (res.recommendedCarbsGrams > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Odporúčané sacharidy: ${res.recommendedCarbsGrams} g", color = textColor)
                    }
                }
            }
        }
    }
}

data class GlycemiaCheckResult(
    val isSafeToExercise: Boolean,
    val warningMessage: String,
    val recommendedCarbsGrams: Int
)

fun evaluatePreWorkoutGlycemia(
    currentGlycemia: Double,
    activeInsulin: Boolean,
    plannedDurationMinutes: Int
): GlycemiaCheckResult {
    return when {
        currentGlycemia < 3.9 -> {
            GlycemiaCheckResult(
                isSafeToExercise = false,
                warningMessage = "Glykémia je nízka (hypoglykémia). Pred cvičením nutne zjedzte rýchle sacharidy.",
                recommendedCarbsGrams = 15
            )
        }
        currentGlycemia in 3.9..5.0 && activeInsulin -> {
            GlycemiaCheckResult(
                isSafeToExercise = true,
                warningMessage = "Glykémia je na dolnej hranici a pôsobí aktívny inzulín. Odporúčame preventívnu malú svačinu.",
                recommendedCarbsGrams = 10
            )
        }
        currentGlycemia > 13.9 -> {
            GlycemiaCheckResult(
                isSafeToExercise = false,
                warningMessage = "Glykémia je príliš vysoká (riziko ketoacidózy pri prítomnosti ketónov). Cvičenie odložte.",
                recommendedCarbsGrams = 0
            )
        }
        else -> {
            GlycemiaCheckResult(
                isSafeToExercise = true,
                warningMessage = "Glykémia je v poriadku. Môžete začať cvičiť.",
                recommendedCarbsGrams = 0
            )
        }
    }
}
