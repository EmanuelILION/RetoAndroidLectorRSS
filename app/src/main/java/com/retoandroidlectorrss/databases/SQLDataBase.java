package com.retoandroidlectorrss.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import static com.retoandroidlectorrss.databases.SQLDataBase.DatosTabla.DATABASE_NAME;
import static com.retoandroidlectorrss.databases.SQLDataBase.DatosTabla.DATABASE_VERSION;

public class SQLDataBase extends SQLiteOpenHelper {

    public static abstract class DatosTabla implements BaseColumns {

        public static final String TABLA_NOTICIAS = "noticias";
        public static final String NOTICIA_COLUMNA_ID = "id";
        public static final String NOTICIA_NODE = "fuente";
        public static final String NOTICIA_COLUMNA_TITULO = "titulo";
        public static final String NOTICIA_COLUMNA_DETALLE = "detalle";
        public static final String NOTICIA_COLUMNA_FECHA = "fecha";
        public static final String NOTICIA_COLUMNA_IMAGE_URL = "image";
        public static final String NOTICIA_COLUMNA_URL = "url";

        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = " ,";

        //promos(id,id_res,url,nombre)
        private static final String CREAR_TABLA_NOTICIAS =
                "CREATE TABLE " + DatosTabla.TABLA_NOTICIAS + " (" +
                        DatosTabla.NOTICIA_COLUMNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        DatosTabla.NOTICIA_NODE + TEXT_TYPE + COMMA_SEP +
                        DatosTabla.NOTICIA_COLUMNA_TITULO + TEXT_TYPE + COMMA_SEP +
                        DatosTabla.NOTICIA_COLUMNA_DETALLE + TEXT_TYPE + COMMA_SEP +
                        DatosTabla.NOTICIA_COLUMNA_FECHA + " DATETIME," +
                        DatosTabla.NOTICIA_COLUMNA_IMAGE_URL + TEXT_TYPE + COMMA_SEP +
                        DatosTabla.NOTICIA_COLUMNA_URL + TEXT_TYPE + ")";

        private static final String SQL_DELETE_ENTRIES_NOTICIAS = "DROP TABLE IF EXISTS" + DatosTabla.TABLA_NOTICIAS;

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "RetoRss.db";
    }

    public SQLDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatosTabla.CREAR_TABLA_NOTICIAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Delete
        db.execSQL(DatosTabla.SQL_DELETE_ENTRIES_NOTICIAS);

        //Create
        db.execSQL(DatosTabla.CREAR_TABLA_NOTICIAS);

        onCreate(db);
    }
}
