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
            MainRaffleScreen(this)
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
fun MainRaffleScreen(activity: Activity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(text = "Raffles", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(activity, CreateRaffleScreen::class.java)
                activity.startActivity(intent)
            }
        ) {
            Text("New")
        }

        Spacer(modifier = Modifier.height(16.dp))

        RaffleListScreen { raffleId ->
            val intent = Intent(activity, SelectNumberScreen::class.java)
            intent.putExtra("raffleId", raffleId)
            activity.startActivity(intent)
        }
    }
}
@Composable
fun RaffleListScreen(onRaffleClick: (Int) -> Unit) {
    val context = LocalContext.current
    val db = remember { DataBase(context, "rafflesDB", null, 1) }
    val raffles = remember { mutableStateListOf<Rifa>() }
    var searchNameText by remember { mutableStateOf("") }
    var searchDateText by remember { mutableStateOf("") }

    fun searchRaffles() {
        val cursor = when {
            searchNameText.isNotEmpty() -> db.searchRafflesByName(searchNameText)
            searchDateText.isNotEmpty() -> db.searchRafflesByDate(searchDateText)
            else -> db.getAllRaffles()
        }

        val raffleList = mutableListOf<Rifa>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val name = cursor.getString(1)
            val enrolled = cursor.getString(2)
            val date = cursor.getString(3)
            val matrix = cursor.getString(4)
            raffleList.add(Rifa(id, name, enrolled, date, matrix))
        }
        cursor.close()
        raffles.clear()
        raffles.addAll(raffleList)
    }

    LaunchedEffect(Unit) {
        searchRaffles()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = searchNameText,
            onValueChange = {
                searchNameText = it
                searchDateText = "" // Clear date search
                searchRaffles()
            },
            label = { Text("Search by name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = searchDateText,
            onValueChange = {
                searchDateText = it
                searchNameText = "" // Clear name search
                searchRaffles()
            },
            label = { Text("Search by date (e.g., 2025-05-08)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Raffle List", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        if (raffles.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("No raffles available")
        } else {
            LazyColumn {
                items(raffles) { raffle ->
                    RaffleItem(raffle = raffle, onClick = { onRaffleClick(raffle.id) })
                }
            }
        }
    }
}
@Composable
fun RaffleItem(raffle: Rifa, onClick: () -> Unit) {
    val context = LocalContext.current
    val db = remember { DataBase(context, "rafflesDB", null, 1) }
    val enrolledCount = db.countEnrolled(raffle.matriz)
    val winningNumber = db.getWinningNumberById(raffle.id)
    val activity = (context as? Activity)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(text = raffle.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = "Inscritos: $enrolledCount")
        Text(text = "Fecha: ${raffle.fecha}")
        if (winningNumber != -1) {
            Text(text = "🎉 Ganador: $winningNumber", fontWeight = FontWeight.Bold)
        } else {
            Text(text = "No hay ganador", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            db.deleteRaffleById(raffle.id)
            activity?.recreate()  // To reload the list
        }) {
            Text("Eliminar")
        }
    }
}