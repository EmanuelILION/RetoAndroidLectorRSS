package com.retoandroidlectorrss.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.retoandroidlectorrss.databases.SQLDataBase.DatosTabla;
import com.retoandroidlectorrss.models.ReadRss;

import java.util.List;

public class OperationsBBDD {
    public static void insertNewItems(SQLiteDatabase dataBase, List<ReadRss> items, String node) {
        for (ReadRss item : items) {
            if (!exist(item, dataBase)) {
                ContentValues insertNewRow = new ContentValues();

                insertNewRow.put(DatosTabla.NOTICIA_COLUMNA_TITULO, item.getTitle());
                insertNewRow.put(DatosTabla.NOTICIA_NODE, node);
                insertNewRow.put(DatosTabla.NOTICIA_COLUMNA_DETALLE, item.getDescription());
                insertNewRow.put(DatosTabla.NOTICIA_COLUMNA_FECHA, adjustDateTime(item.getDate()));
                insertNewRow.put(DatosTabla.NOTICIA_COLUMNA_IMAGE_URL, item.getImage());
                insertNewRow.put(DatosTabla.NOTICIA_COLUMNA_URL, item.getLink());

                dataBase.insert(DatosTabla.TABLA_NOTICIAS, null, insertNewRow);
            }
        }
    }

    public static List<ReadRss> allNews(SQLiteDatabase dataBase, List<ReadRss> listItems, String node) {
        String order = DatosTabla.NOTICIA_COLUMNA_FECHA;
        String[] conditions = {node};
        String selection = DatosTabla.NOTICIA_NODE;
        String[] targets = {DatosTabla.NOTICIA_COLUMNA_TITULO, DatosTabla.NOTICIA_COLUMNA_DETALLE,
                DatosTabla.NOTICIA_COLUMNA_IMAGE_URL, DatosTabla.NOTICIA_COLUMNA_URL, DatosTabla.NOTICIA_COLUMNA_FECHA};
        Cursor allRows = dataBase.query(DatosTabla.TABLA_NOTICIAS, targets, selection + "=?", conditions, null, null, order + " DESC");

        allRows.moveToFirst();
        while (allRows.moveToNext()) {
            ReadRss readRss = new ReadRss();
            readRss.setTitle(allRows.getString(0));
            readRss.setDescription(allRows.getString(1));
            readRss.setImage(allRows.getString(2));
            readRss.setLink(allRows.getString(3));
            readRss.setDate(allRows.getString(4));
            listItems.add(readRss);
        }
        return listItems;
    }

    public static boolean chargedNode(SQLiteDatabase dataBase, String node) {
        String[] targets = {DatosTabla.NOTICIA_NODE};
        Cursor row = dataBase.query(DatosTabla.TABLA_NOTICIAS, targets, null, null, null, null, null);

        if(row.moveToFirst()){
            return true;
        }else{
            return false;
        }
    }

    private static boolean exist(ReadRss item, SQLiteDatabase dataBase) {

        String[] targets = {DatosTabla.NOTICIA_COLUMNA_ID};
        String[] conditions = {item.getTitle()};
        Cursor row = dataBase.query(DatosTabla.TABLA_NOTICIAS, targets,
                DatosTabla.NOTICIA_COLUMNA_TITULO + "=?", conditions, null, null, null, null);
        //Cursor row = dataBase.rawQuery("SELECT id FROM noticias where titulo=?", conditions);

        if (row.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    private static String adjustDateTime(String dateTime) {
        String aux = dateTime.substring(dateTime.indexOf(" ") + 1);
        aux = aux.substring(0, 10 + 1);

        String auxMonth = aux.substring(aux.indexOf(" ") + 1, aux.lastIndexOf(" "));
        String month = "";
        String day = aux.substring(0, aux.indexOf(" "));
        String year = aux.substring(aux.lastIndexOf(" ") + 1);
        switch (auxMonth) {
            case "Jan":
                month = "01";
                break;
            case "Feb":
                month = "02";
                break;
            case "Mar":
                month = "03";
                break;
            case "Apr":
                month = "04";
                break;
            case "May":
                month = "05";
                break;
            case "Jun":
                month = "06";
                break;
            case "Jul":
                month = "07";
                break;
            case "Aug":
                month = "08";
                break;
            case "Sep":
                month = "09";
                break;
            case "Oct":
                month = "10";
                break;
            case "Nov":
                month = "11";
                break;
            case "Dec":
                month = "12";
                break;
            default:
                break;
        }
        return (year + "-" + month + "-" + day);
    }

}
