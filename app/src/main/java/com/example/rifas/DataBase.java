package com.example.rifas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBase extends SQLiteOpenHelper {

    public DataBase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE raffles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "enrolled TEXT, " +
                "matrix TEXT NOT NULL, " +
                "winningNumber INTEGER" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS raffles");
        onCreate(db);
    }

    public long insertRaffle(String name, String date, String enrolled, String matrixJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("date", date);
        values.put("enrolled", enrolled);
        values.put("matrix", matrixJson);
        long result = db.insert("raffles", null, values);
        db.close();
        return result;
    }

    public String getMatrixById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT matrix FROM raffles WHERE id = ?", new String[]{String.valueOf(id)});
        String matrix = "";
        if (cursor.moveToFirst()) {
            matrix = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return matrix;
    }

    public void updateMatrix(int id, String newMatrix) {
        int enrolledCount = countEnrolled(newMatrix);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("matrix", newMatrix);
        values.put("enrolled", String.valueOf(enrolledCount));
        db.update("raffles", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Cursor getAllRaffles() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, name, enrolled, date, matrix FROM raffles", null);
    }

    public int countEnrolled(String matrix) {
        if (matrix == null || matrix.isEmpty() || matrix.equals("[]")) return 0;

        String cleaned = matrix.replace("(", "").replace(")", "");
        String[] numbers = cleaned.split(",");

        int count = 0;
        for (String number : numbers) {
            if (number.trim().equals("1")) {
                count++;
            }
        }
        return count;
    }

    public void setWinningNumber(int id, int winningNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("winningNumber", winningNumber);
        db.update("raffles", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int getWinningNumberById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT winningNumber FROM raffles WHERE id = ?", new String[]{String.valueOf(id)});
        int winningNumber = -1;
        if (cursor.moveToFirst()) {
            winningNumber = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return winningNumber;
    }

    /** Method to get the raffle's name by its ID */
    public String getRaffleNameById(int raffleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM raffles WHERE id = ?", new String[]{String.valueOf(raffleId)});

        if (cursor != null && cursor.moveToFirst()) {
            String raffleName = cursor.getString(cursor.getColumnIndex("name"));
            cursor.close();
            return raffleName;
        }

        cursor.close();
        return null;  // If the name is not found, returns null
    }

    public Cursor searchRafflesByName(String searchedName) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, name, enrolled, date, matrix FROM raffles WHERE name LIKE ?",
                new String[]{"%" + searchedName + "%"});
    }

    public void deleteRaffleById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("raffles", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Cursor searchRafflesByDate(String searchedDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT id, name, enrolled, date, matrix FROM raffles WHERE date LIKE ?",
                new String[]{"%" + searchedDate + "%"}
        );
    }
}