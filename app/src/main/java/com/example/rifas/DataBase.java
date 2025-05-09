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
        db.execSQL("CREATE TABLE rifas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "fecha TEXT NOT NULL, " +
                "inscritos TEXT, " +
                "matriz TEXT NOT NULL, " +
                "numeroGanador INTEGER" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS rifas");
        onCreate(db);
    }

    public long insertarRifa(String nombre, String fecha, String inscritos, String matrizJson, Integer ganador) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("fecha", fecha);
        values.put("inscritos", inscritos);
        values.put("matriz", matrizJson);
        values.put("numeroGanador", ganador);
        long result = db.insert("rifas", null, values);
        db.close();
        return result;
    }

    public String obtenerMatrizPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT matriz FROM rifas WHERE id = ?", new String[]{String.valueOf(id)});
        String matriz = "";
        if (cursor.moveToFirst()) {
            matriz = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return matriz;
    }

    public void actualizarMatriz(int id, String nuevaMatriz) {
        int inscritos = contarInscritos(nuevaMatriz);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("matriz", nuevaMatriz);
        values.put("inscritos", String.valueOf(inscritos));
        db.update("rifas", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Cursor obtenerTodasLasRifas() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, nombre, inscritos, fecha, matriz FROM rifas", null);
    }

    public int contarInscritos(String matriz) {
        if (matriz == null || matriz.isEmpty() || matriz.equals("[]")) return 0;

        String limpia = matriz.replace("(", "").replace(")", "");
        String[] numeros = limpia.split(",");

        int contador = 0;
        for (String numero : numeros) {
            if (numero.trim().equals("1")) {
                contador++;
            }
        }
        return contador;
    }

    public void establecerNumeroGanador(int id, int numeroGanador) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("numeroGanador", numeroGanador);
        db.update("rifas", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int obtenerNumeroGanadorPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT numeroGanador FROM rifas WHERE id = ?", new String[]{String.valueOf(id)});
        int numeroGanador = 0;
        if (cursor.moveToFirst()) {
            numeroGanador = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return numeroGanador;
    }

    // Método para obtener el nombre de la rifa por su ID
    public String obtenerNombreRifaPorId(int rifaId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre FROM rifas WHERE id = ?", new String[]{String.valueOf(rifaId)});

        String nombreRifa = null;
        if (cursor.moveToFirst()) {
            nombreRifa = cursor.getString(0); // Primera columna
        }

        cursor.close();
        db.close();
        return nombreRifa;
    }


    public Cursor buscarRifasPorNombre(String nombreBuscado) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, nombre, inscritos, fecha, matriz, numeroGanador FROM rifas WHERE nombre LIKE ?",
                new String[]{"%" + nombreBuscado + "%"});
    }

    public Cursor buscarRifasPorFecha(String fechaBuscada) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT id, nombre, inscritos, fecha, matriz FROM rifas WHERE fecha LIKE ?",
                new String[]{"%" + fechaBuscada + "%"}
        );
    }

    public void eliminarRifaPorId(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("rifas", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Integer obtenerGanadorPorId(int rifaId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT numeroGanador FROM rifas WHERE id = ?", new String[]{String.valueOf(rifaId)});
        Integer ganador = null;
        if (cursor.moveToFirst()) {
            if (!cursor.isNull(0)) {
                ganador = cursor.getInt(0);
            }
        }
        cursor.close();
        db.close();
        return ganador;
    }

    public boolean guardarGanador(int rifaId, int ganador) {
        Integer ganadorExistente = obtenerGanadorPorId(rifaId);
        if (ganadorExistente != null) {
            return false; // Ya existe un ganador
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("numeroGanador", ganador);
        int filas = db.update("rifas", values, "id = ?", new String[]{String.valueOf(rifaId)});
        db.close();
        return filas > 0;
    }



}