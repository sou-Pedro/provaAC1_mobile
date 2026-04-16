package com.example.provaac1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contatos.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "contatos";
    public static final String COLUMN_ID = "_id"; // Using _id for SimpleCursorAdapter compatibility
    public static final String COLUMN_NOME = "nome";
    public static final String COLUMN_TELEFONE = "telefone";
    public static final String COLUMN_EMAIL = "email";
    public // Mude de 1 para 2 para que os campos novos (telefone, categoria, etc) sejam criados
    private static final int DATABASE_VERSION = 2;// Mude de 1 para 2 para que os campos novos (telefone, categoria, etc) sejam criados
    private static final int DATABASE_VERSION// Mude de 1 para 2 para que os campos novos (telefone, categoria, etc) sejam criados
    static final String COLUMN_CATEGORIA = "categoria";
    public static final String COLUMN_CIDADE = "cidade";
    public static final String COLUMN_FAVORITO = "favorito";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NOME + " TEXT, "
                + COLUMN_TELEFONE + " TEXT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_CATEGORIA + " TEXT, "
                + COLUMN_CIDADE + " TEXT, "
                + COLUMN_FAVORITO + " INTEGER)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long inserirContato(String nome, String telefone, String email, String categoria, String cidade, int favorito) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOME, nome);
        values.put(COLUMN_TELEFONE, telefone);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_CATEGORIA, categoria);
        values.put(COLUMN_CIDADE, cidade);
        values.put(COLUMN_FAVORITO, favorito);
        return db.insert(TABLE_NAME, null, values);
    }

    public Cursor listarContatos(String categoria, boolean apenasFavoritos, String busca) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        if (categoria != null && !categoria.equals("Todos")) {
            selection.append(COLUMN_CATEGORIA).append(" = ?");
            selectionArgs.add(categoria);
        }

        if (apenasFavoritos) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COLUMN_FAVORITO).append(" = 1");
        }

        if (busca != null && !busca.isEmpty()) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COLUMN_NOME).append(" LIKE ?");
            selectionArgs.add("%" + busca + "%");
        }

        return db.query(TABLE_NAME, null,
                selection.length() > 0 ? selection.toString() : null,
                selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]),
                null, null, COLUMN_NOME + " ASC");
    }

    public int atualizarContato(long id, String nome, String telefone, String email, String categoria, String cidade, int favorito) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOME, nome);
        values.put(COLUMN_TELEFONE, telefone);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_CATEGORIA, categoria);
        values.put(COLUMN_CIDADE, cidade);
        values.put(COLUMN_FAVORITO, favorito);
        return db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int excluirContato(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
