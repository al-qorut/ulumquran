package smk.adzikro.indextemaquran.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * Created by server on 12/14/16.
 */

public class BookmarkHelper {
    private static int VERSI=1;
    private static String DATABASE = Fungsi.PATH_DATABASE()+"bookmark.db";
    private static Context context;
    private static SQLiteDatabase db;

    public BookmarkHelper(Context context) {
        BookmarkDatabase bookmarkDatabase = new BookmarkDatabase(context);
        this.db = bookmarkDatabase.getWritableDatabase();
        this.context = context;
    }

    private class BookmarkDatabase extends SQLiteOpenHelper {
        public BookmarkDatabase(Context context) {
            super(context, DATABASE, null, VERSI);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create TABLE IF NOT EXISTS bookmarks (_ID INTEGER PRIMARY KEY " +
                    "AUTOINCREMENT, surat int, ayat int, page int, aksi int, isi text, tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }

    public  long addBook(int surat, int ayat, int page, int aksi, String isi){
        ContentValues data = new ContentValues();
        data.put(BookmarksTable.SURAT, surat);
        data.put(BookmarksTable.AYAT, ayat);
        data.put(BookmarksTable.PAGE, page);
        data.put(BookmarksTable.AKSI, aksi);
        data.put(BookmarksTable.ISI, isi);
        return db.insert(BookmarksTable.TABLE_NAME,null,data);
    }

    public  long getBookId(int surat, int ayat, int page, int aksi){
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + BookmarksTable.TABLE_NAME +
                    " where page= " + page + " and aksi= " + aksi+" and ayat="+ayat+" and surat="+surat, null);
            if(cursor!=null && cursor.moveToNext()){
                return cursor.getLong(0);
            }
        }catch (Exception e) {
            // swallow the error for now
        } finally {
            cursor.close();
        }
        return -1;
    }

    public boolean isPageBookmarked(int page){
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + BookmarksTable.TABLE_NAME +
                    " where page= " + page, null);
            if(cursor!=null && cursor.moveToNext()){
                return true;
            }
        }catch (Exception e) {
            // swallow the error for now
        } finally {
            cursor.close();
        }
        return false;
    }

    public  long addBookmarkIfNotExists(Integer sura, Integer ayah, int page, int aksi, String isi) {
        long bookmarkId = getBookId(sura, ayah, page, aksi);
        if (bookmarkId < 0) {
            bookmarkId = addBook(sura, ayah, page, aksi, isi);
        }
        return bookmarkId;
    }
    public  long addBookmarkTadarrus(Integer sura, Integer ayah, int page, int aksi, String isi) {
       // long bookmarkId = getBookId(sura, ayah, page, aksi);
        if (aksi < 0) hapusTadarrus();
            return addBook(sura, ayah, page, aksi, isi);
    }

    public  boolean hapusBook(long id){
        return db.delete(BookmarksTable.TABLE_NAME,"_ID="+id,null)==1;
    }

    public  boolean hapusTadarrus(){
        return db.delete(BookmarksTable.TABLE_NAME,"aksi=-1",null)==1;
    }

    public int getCount(){
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select count(*) as j from " + BookmarksTable.TABLE_NAME
                    , null);
            if(cursor!=null && cursor.moveToNext()){
                return cursor.getInt(0);
            }
        }catch (Exception e) {
            // swallow the error for now
        } finally {
            cursor.close();
        }
        return 0;
    }

    public  boolean hapusBookPage(int page){
        return db.delete(BookmarksTable.TABLE_NAME,"page="+page,null)==1;
    }
    public long addRemoveBookmark(Integer sura, Integer ayah, int page, int aksi, String isi) {
        long bookmarkId = getBookId(sura, ayah, page, aksi);
        if (bookmarkId < 0) {
            bookmarkId = addBook(sura, ayah, page, aksi, isi);
        }
        return bookmarkId;
    }

    public ArrayList<HashMap<String, String>> getListBook(int aksi) {
        ArrayList<HashMap<String, String>> alist = new ArrayList<HashMap<String, String>>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from "+BookmarksTable.TABLE_NAME+
                    " where aksi="+aksi, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("no", String.valueOf(cursor.getPosition() + 1));
                    hashMap.put("surat", cursor.getString(cursor.getColumnIndex("surat")));
                    hashMap.put("ayat", cursor.getString(cursor.getColumnIndex("ayat")));
                    hashMap.put("page", cursor.getString(cursor.getColumnIndex("page")));
                    hashMap.put("aksi", cursor.getString(cursor.getColumnIndex("aksi")));
                    alist.add(hashMap);
                }
            }
        }catch (Exception e) {
            // swallow the error for now
        } finally {
            cursor.close();
        }

        return alist;
    }

    public Cursor getListBookAksi(int aksi){
        return db.rawQuery("select * from "+BookmarksTable.TABLE_NAME+" where aksi="+aksi,null);
    }
    static class BookmarksTable {
        static final String TABLE_NAME = "bookmarks";
        static final String ID = "_ID";
        static final String SURAT = "surat";
        static final String AYAT = "ayat";
        static final String PAGE = "page";
        static final String AKSI = "aksi";
        static final String ISI = "isi";
        static final String ADDED_DATE = "tanggal";
    }
}
