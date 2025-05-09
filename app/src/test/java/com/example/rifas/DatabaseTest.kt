package com.example.rifas

import android.content.Context
import android.database.Cursor
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class DataBaseTest {
    private lateinit var db: DataBase
    private lateinit var context: Context
    private val dbName = "testRafflesDB"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
        db = DataBase(context, dbName, null, 1)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun insertarYObtenerTodasLasRifas() {
        val fmt = DateTimeFormatter.ISO_DATE
        val date1 = LocalDate.now().format(fmt)
        val date2 = LocalDate.now().plusDays(1).format(fmt)
        val id1 = db.insertarRifa("Alpha", date1, "0", "(0,0)", null)
        val id2 = db.insertarRifa("Beta", date2, "0", "(1,1)", null)
        assertTrue(id1 > 0)
        assertTrue(id2 > id1)

        val cursor: Cursor = db.obtenerTodasLasRifas()
        val names = mutableListOf<String>()
        while (cursor.moveToNext()) {
            names.add(cursor.getString(1))
        }
        cursor.close()
        assertEquals(2, names.size)
        assertTrue(names.containsAll(listOf("Alpha", "Beta")))
    }

    @Test
    fun contarInscritosYOperacionesConMatriz() {
        val matrix = "(1,0,1,1)"
        val count = db.contarInscritos(matrix)
        assertEquals(3, count)

        val id = db.insertarRifa("Gamma", "2025-01-01", "0", matrix, null)
        val fetched = db.obtenerMatrizPorId(id.toInt())
        assertEquals(matrix, fetched)

        val newMatrix = "(0,0,0,0)"
        db.actualizarMatriz(id.toInt(), newMatrix)
        val updated = db.obtenerMatrizPorId(id.toInt())
        assertEquals(newMatrix, updated)
    }

    @Test
    fun obtenerNombreDeRifaPorId() {
        val id = db.insertarRifa("Echo", "2025-01-03", "0", "(0)", null)
        val name = db.obtenerNombreRifaPorId(id.toInt())
        assertEquals("Echo", name)
    }

    @Test
    fun buscarPorNombreYFecha() {
        val fmt = DateTimeFormatter.ISO_DATE
        val today = LocalDate.now().format(fmt)
        db.insertarRifa("SearchMe", today, "0", "(0)", null)
        db.insertarRifa("Other", "1999-12-31", "0", "(0)", null)

        val byName = db.buscarRifasPorNombre("SearchMe")
        assertTrue(byName.moveToFirst())
        assertEquals("SearchMe", byName.getString(1))
        byName.close()

        val byDate = db.buscarRifasPorFecha(today)
        assertTrue(byDate.moveToFirst())
        assertEquals(today, byDate.getString(3))
        byDate.close()
    }

    @Test
    fun eliminarRifaPorId() {
        val id = db.insertarRifa("ToDelete", "2025-01-04", "0", "(0)", null)
        val intId = id.toInt()

        val before = db.buscarRifasPorNombre("ToDelete")
        assertTrue(before.moveToFirst())
        before.close()

        db.eliminarRifaPorId(intId)

        val after = db.buscarRifasPorNombre("ToDelete")
        assertFalse(after.moveToFirst())
        after.close()
    }

    @Test
    fun operacionesConNumeroGanador() {
        val id = db.insertarRifa("Winnerless", "2025-01-05", "0", "(0)", null)
        val raffleId = id.toInt()

        val noWinner = db.obtenerGanadorPorId(raffleId)
        assertNull(noWinner)

        val saved = db.guardarGanador(raffleId, 42)
        assertTrue(saved)

        val storedWinner = db.obtenerGanadorPorId(raffleId)
        assertNotNull(storedWinner)
        assertEquals(42, storedWinner)

        val overwriteAttempt = db.guardarGanador(raffleId, 99)
        assertFalse(overwriteAttempt)

        val finalWinner = db.obtenerGanadorPorId(raffleId)
        assertEquals(42, finalWinner)
    }
}
