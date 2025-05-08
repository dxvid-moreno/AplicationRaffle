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

    val matrizJson = remember {
        db.obtenerMatrizPorId(rifaId)
    }

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
    var checked by remember { mutableStateOf(false) }

    // Obtener el nombre de la rifa
    val nombreRifa = remember { db.obtenerNombreRifaPorId(rifaId) ?: "Rifa desconocida" }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("$nombreRifa", fontSize = 24.sp, fontWeight = FontWeight.Bold)

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
            value = ganador,
            onValueChange = { ganador = it },
            label = { Text("Boleto ganador") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Inhabilitar")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = checked, onCheckedChange = { checked = it })
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
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
                Text("Eliminar", color = Color.White)
            }

            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            ) {
                Text("Volver al inicio", color = Color.White)
            }

        }


    }
}

