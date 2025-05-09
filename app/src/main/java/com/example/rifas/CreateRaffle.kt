package com.example.rifas


import androidx.activity.enableEdgeToEdge
import android.R.attr.left
import android.app.Activity
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
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
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import android.content.Intent



class CreateRaffleScreen : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreateNewRaffleScreen()
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateNewRaffleScreen() {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = LocalDate.of(year, month + 1, dayOfMonth)
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create new Raffle", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Raffle date", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { showDatePicker = true }) {
            Text(text = date.toString())
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val db = DataBase(context, "rafflesDB", null, 1)

                val enrolled = "0"
                val matrixJson = List(10) { List(10) { 0 } }
                    .joinToString(",") { row -> "(" + row.joinToString(",") + ")" }

                val result = db.insertRaffle(name, date.toString(), enrolled, matrixJson)

                if (result == -1L) {
                    Toast.makeText(context, "Raffle not saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Raffle saved successfully", Toast.LENGTH_SHORT).show()

                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)

                    if (context is Activity) {
                        (context as Activity).finish()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5E5E))
        ) {
            Text("Save", color = Color.White)
        }
    }

    if (showDatePicker) {
        datePickerDialog.show()
        showDatePicker = false
    }
}