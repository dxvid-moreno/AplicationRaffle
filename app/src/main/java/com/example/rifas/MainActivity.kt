package com.example.rifas

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rifas.ui.theme.Rifa

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen(this)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }


    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
    }


    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart called")
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
    }
}
@Composable
fun MainScreen(activity: Activity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(text = "Rifas", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(activity, CrearRifa::class.java)
                activity.startActivity(intent)
            }
        ) {
            Text("Nuevo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        RifaListScreen { rifaId ->
            val intent = Intent(activity, SelectNumber::class.java)
            intent.putExtra("rifaId", rifaId)
            activity.startActivity(intent)
        }
    }
}
@Composable
fun RifaListScreen(onRifaClick: (Int) -> Unit) {
    val context = LocalContext.current
    val db = remember { DataBase(context, "rifasDB", null, 1) }
    val rifas = remember { mutableStateListOf<Rifa>() }
    var textoBusquedaNombre by remember { mutableStateOf("") }
    var textoBusquedaFecha by remember { mutableStateOf("") }

    fun buscar() {
        val cursor = when {
            textoBusquedaNombre.isNotEmpty() -> db.buscarRifasPorNombre(textoBusquedaNombre)
            textoBusquedaFecha.isNotEmpty() -> db.buscarRifasPorFecha(textoBusquedaFecha)
            else -> db.obtenerTodasLasRifas()
        }

        val lista = mutableListOf<Rifa>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val nombre = cursor.getString(1)
            val inscritos = cursor.getString(2)
            val fecha = cursor.getString(3)
            val matriz = cursor.getString(4)
            lista.add(Rifa(id, nombre, inscritos, fecha, matriz))
        }
        cursor.close()
        rifas.clear()
        rifas.addAll(lista)
    }

    LaunchedEffect(Unit) {
        buscar()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = textoBusquedaNombre,
            onValueChange = {
                textoBusquedaNombre = it
                textoBusquedaFecha = "" // Limpiar búsqueda por fecha
                buscar()
            },
            label = { Text("Buscar por nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = textoBusquedaFecha,
            onValueChange = {
                textoBusquedaFecha = it
                textoBusquedaNombre = "" // Limpiar búsqueda por nombre
                buscar()
            },
            label = { Text("Buscar por fecha (ej. 2025-05-08)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Lista de Rifas", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        if (rifas.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("No hay rifas disponibles")
        } else {
            LazyColumn {
                items(rifas) { rifa ->
                    RifaItem(rifa = rifa, onClick = { onRifaClick(rifa.id) })
                }
            }
        }
    }
}




@Composable
fun RifaItem(rifa: Rifa, onClick: () -> Unit) {
    val context = LocalContext.current
    val db = remember { DataBase(context, "rifasDB", null, 1) }
    val inscritos = db.contarInscritos(rifa.matriz)
    val ganador = db.obtenerNumeroGanadorPorId(rifa.id)
    val activity = (context as? Activity)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(text = rifa.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = "Inscritos: $inscritos")
        Text(text = "Fecha: ${rifa.fecha}")
        if (ganador != -1) {
            Text(text = "🎉 Ganador: $ganador", fontWeight = FontWeight.Bold)
        }else{
            Text(text = "No hay ganador", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            db.eliminarRifaPorId(rifa.id)
            activity?.recreate()  // Para recargar la lista
        }) {
            Text("Eliminar")
        }
    }
}




