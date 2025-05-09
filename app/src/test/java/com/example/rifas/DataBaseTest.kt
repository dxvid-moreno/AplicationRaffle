package com.example.rifas

import android.content.Context
import android.database.Cursor
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.os.Build
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class DataBaseTest {
    private lateinit var db: DataBase
    private lateinit var context: Context
    private val dbName = "testRafflesDB"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Create a fresh test database
        context.deleteDatabase(dbName)
        db = DataBase(context, dbName, null, 1)
    }

    @After
    fun tearDown() {
        // Clean up after tests
        context.deleteDatabase(dbName)
    }

    @Test
    fun testInsertAndGetAllRaffles() {
        // Insert two raffles
        val fmt = DateTimeFormatter.ISO_DATE
        val date1 = LocalDate.now().format(fmt)
        val date2 = LocalDate.now().plusDays(1).format(fmt)
        val id1 = db.insertRaffle("Alpha", date1, "0", "(0,0)")
        val id2 = db.insertRaffle("Beta", date2, "0", "(1,1)")
        assertTrue(id1 > 0)
        assertTrue(id2 > id1)

        // Retrieve all raffles
        val cursor: Cursor = db.getAllRaffles()
        val names = mutableListOf<String>()
        while (cursor.moveToNext()) {
            names.add(cursor.getString(1)) // name column
        }
        cursor.close()
        assertEquals(2, names.size)
        assertTrue(names.containsAll(listOf("Alpha", "Beta")))
    }

    @Test
    fun testCountEnrolledAndMatrixOperations() {
        // Test countEnrolled logic
        val matrix = "(1,0,1,1)"
        val count = db.countEnrolled(matrix)
        assertEquals(3, count)

        // Insert and update matrix
        val id = db.insertRaffle("Gamma", "2025-01-01", "0", matrix)
        val fetched = db.getMatrixById(id.toInt())
        assertEquals(matrix, fetched)

        val newMatrix = "(0,0,0,0)"
        db.updateMatrix(id.toInt(), newMatrix)
        val updated = db.getMatrixById(id.toInt())
        assertEquals(newMatrix, updated)
    }

    @Test
    fun testGetRaffleNameById() {
        // Insert raffle and fetch its name
        val id = db.insertRaffle("Echo", "2025-01-03", "0", "(0)")
        val name = db.getRaffleNameById(id.toInt())
        assertEquals("Echo", name)
    }

    @Test
    fun testSearchByNameAndDate() {
        // Insert multiple raffles
        val fmt = DateTimeFormatter.ISO_DATE
        val today = LocalDate.now().format(fmt)
        db.insertRaffle("SearchMe", today, "0", "(0)")
        db.insertRaffle("Other", "1999-12-31", "0", "(0)")

        // Search by name
        val byName = db.searchRafflesByName("SearchMe")
        assertTrue(byName.moveToFirst())
        assertEquals("SearchMe", byName.getString(1))
        byName.close()

        // Search by date
        val byDate = db.searchRafflesByDate(today)
        assertTrue(byDate.moveToFirst())
        assertEquals(today, byDate.getString(3))
        byDate.close()
    }

    @Test
    fun testDeleteRaffleById() {
        val id = db.insertRaffle("ToDelete", "2025-01-04", "0", "(0)")
        val intId = id.toInt()
        // Ensure exists
        val before = db.searchRafflesByName("ToDelete")
        assertTrue(before.moveToFirst())
        before.close()

        // Delete and verify
        db.deleteRaffleById(intId)
        val after = db.searchRafflesByName("ToDelete")
        assertFalse(after.moveToFirst())
        after.close()
    }

}
