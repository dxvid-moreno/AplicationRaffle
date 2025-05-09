package com.example.rifas

import android.R.attr.left
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import android.content.Intent


class SelectNumberScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val raffleId = intent.getIntExtra("raffleId", -1)

        setContent {
            RaffleGridScreen(raffleId = raffleId)
        }
    }
}


@Composable
fun RaffleGridScreen(raffleId: Int) {
    val context = LocalContext.current
    val db = remember { DataBase(context, "rafflesDB", null, 1) }

    val matrixJson = remember {
        db.getMatrixById(raffleId)
    }

    val initialMatrix = remember {
        if (matrixJson.isNullOrBlank() || matrixJson == "[]") {
            List(10) { List(10) { 0 } }
        } else {
            matrixJson
                .split("),(")
                .map {
                    it.replace("(", "")
                        .replace(")", "")
                        .split(",")
                        .map { num -> num.trim().toInt() }
                }
        }
    }


    var selectedNumbers by remember { mutableStateOf(initialMatrix) }

    var winningTicket by remember { mutableStateOf("") }
    var disableToggle by remember { mutableStateOf(false) }

    // Get the raffle name
    val raffleName = remember { db.getRaffleNameById(raffleId) ?: "Unknown Raffle" }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("$raffleName", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        LazyVerticalGrid(
            columns = GridCells.Fixed(10),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            items(100) { index ->
                val row = index / 10
                val col = index % 10
                val isSelected = selectedNumbers[row][col] == 1

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFFE91E63) else Color.LightGray)
                        .clickable {
                            selectedNumbers = selectedNumbers.toMutableList().apply {
                                val updatedRow = this[row].toMutableList().apply {
                                    this[col] = if (this[col] == 1) 0 else 1
                                }
                                this[row] = updatedRow
                            }
                        }
                ) {
                    Text(
                        text = index.toString(),
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }

        OutlinedTextField(
            value = winningTicket,
            onValueChange = { winningTicket = it },
            label = { Text("Winning ticket") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Disable")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = disableToggle, onCheckedChange = { disableToggle = it })
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Button(
                onClick = {
                    val newMatrix = selectedNumbers.joinToString(",") {
                        "(" + it.joinToString(",") + ")"
                    }
                    db.updateMatrix(raffleId, newMatrix)
                    Toast.makeText(context, "Matrix saved successfully", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
            ) {
                Text("Save", color = Color.White)
            }

            Button(
                onClick = {
                    selectedNumbers = List(10) { List(10) { 0 } }
                    val emptyMatrix = selectedNumbers.joinToString(",") {
                        "(" + it.joinToString(",") + ")"
                    }
                    db.updateMatrix(raffleId, emptyMatrix)
                    Toast.makeText(context, "Matrix cleared", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
            ) {
                Text("Clear", color = Color.White)
            }

            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            ) {
                Text("Back to Home", color = Color.White)
            }
        }
    }
}