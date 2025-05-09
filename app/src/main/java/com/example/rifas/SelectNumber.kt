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
import androidx.compose.runtime.LaunchedEffect

class SelectNumber : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rifaId = intent.getIntExtra("rifaId", -1)

        setContent {
            RaffleScreen(rifaId = rifaId)
        }
    }
}

@Composable
fun RaffleScreen(rifaId: Int) {
    val context = LocalContext.current
    val db = remember { DataBase(context, "rifasDB", null, 1) }

    val matrizJson = remember { db.obtenerMatrizPorId(rifaId) }

    val matrizInicial = remember {
        if (matrizJson.isNullOrBlank() || matrizJson == "[]") {
            List(10) { List(10) { 0 } }
        } else {
            matrizJson
                .split("),(")
                .map {
                    it.replace("(", "")
                        .replace(")", "")
                        .split(",")
                        .map { num -> num.trim().toInt() }
                }
        }
    }

    var selectedNumbers by remember { mutableStateOf(matrizInicial) }

    var ganador by remember { mutableStateOf("") }
    var ganadorAsignado by remember { mutableStateOf<Int?>(null) }
    var ganadorError by remember { mutableStateOf("") }

    val nombreRifa = remember { db.obtenerNombreRifaPorId(rifaId) ?: "Rifa desconocida" }

    LaunchedEffect(rifaId) {
        ganadorAsignado = db.obtenerGanadorPorId(rifaId)
        if (ganadorAsignado != null && ganadorAsignado != -1) {
            ganador = ganadorAsignado.toString()
        }
    }

    val isEditable = ganadorAsignado == null || ganadorAsignado == -1

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .padding(top = 40.dp)
    ) {

        Text("$nombreRifa", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(10),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
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
                        .clickable(enabled = isEditable) {
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

        if (isEditable) {
            OutlinedTextField(
                value = ganador,
                onValueChange = {
                    ganador = it
                    ganadorError = ""
                },
                label = { Text("Asignar Boleto Ganador") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                isError = ganadorError.isNotEmpty()
            )

            if (ganadorError.isNotEmpty()) {
                Text(
                    text = ganadorError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Button(
                onClick = {
                    if (ganador.isNotBlank()) {
                        val numeroGanador = ganador.toIntOrNull()
                        if (numeroGanador != null && numeroGanador in 0..99) {
                            val row = numeroGanador / 10
                            val col = numeroGanador % 10
                            if (selectedNumbers[row][col] == 1) {
                                val exito = db.guardarGanador(rifaId, numeroGanador)
                                if (exito) {
                                    ganadorAsignado = numeroGanador
                                    Toast.makeText(context, "Ganador guardado correctamente", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No se pudo guardar el ganador", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                ganadorError = "El número $numeroGanador no ha sido seleccionado en la matriz."
                            }
                        } else {
                            ganadorError = "El número debe estar entre 0 y 99."
                        }
                    } else {
                        ganadorError = "Ingresa un número ganador válido."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Guardar ganador", color = Color.White)
            }


            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Button(
                    onClick = {
                        val nuevaMatriz = selectedNumbers.joinToString(",") {
                            "(" + it.joinToString(",") + ")"
                        }
                        db.actualizarMatriz(rifaId, nuevaMatriz)
                        Toast.makeText(context, "Matriz guardada correctamente", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text("Guardar", color = Color.White)
                }

                Button(
                    onClick = {
                        selectedNumbers = List(10) { List(10) { 0 } }
                        val matrizVacia = selectedNumbers.joinToString(",") {
                            "(" + it.joinToString(",") + ")"
                        }
                        db.actualizarMatriz(rifaId, matrizVacia)
                        Toast.makeText(context, "Matriz limpiada", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text("Limpiar", color = Color.White)
                }
            }
        } else {
            Text(
                "ESTA RIFA YA TIENE GANADOR",
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 24.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Volver al inicio", color = Color.White)
            }
        }
    }
}
