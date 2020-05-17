package smk.adzikro.indextemaquran.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import smk.adzikro.indextemaquran.object.Books;
import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * Created by server on 12/14/16.
 */

public class BookmarkHelper {
    private static int VERSI=2;
    private static String DATABASE;
    private static Context context;
    private static SQLiteDatabase db;

    public BookmarkHelper(Context context) {
        BookmarkDatabase bookmarkDatabase = new BookmarkDatabase(context);
        DATABASE = Fungsi.PATH_DATABASES(context)+"bookmark.db";
        this.context = context;
        this.db = bookmarkDatabase.getWritableDatabase();
    }

    private class BookmarkDatabase extends SQLiteOpenHelper {
        public BookmarkDatabase(Context context) {
            super(context, DATABASE, null, VERSI);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create TABLE IF NOT EXISTS bookmarks (_ID INTEGER PRIMARY KEY " +
                    "AUTOINCREMENT, surat int, ayat int, folder text, isi text, tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }

    public  long addBook(int surat, int ayat, String folder){
        ContentValues data = new ContentValues();
        data.put(BookmarksTable.SURAT, surat);
        data.put(BookmarksTable.AYAT, ayat);
        data.put(BookmarksTable.FOLDER, folder);
        return db.insert(BookmarksTable.TABLE_NAME,null,data);
    }

    public  long getBookId(int surat, int ayat){
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + BookmarksTable.TABLE_NAME +
                    " where ayat="+ayat+" and surat="+surat, null);
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



    public long addRemoveBookmark(Integer sura, Integer ayah, String isi) {
        long bookmarkId = getBookId(sura, ayah);
        if (bookmarkId < 0) {
            bookmarkId = addBook(sura, ayah, isi);
        }else{
            hapusBook(bookmarkId);
        }
        return bookmarkId;
    }


    public  boolean hapusBook(long id){
        return db.delete(BookmarksTable.TABLE_NAME,"_ID="+id,null)==1;
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



    public ArrayList<HashMap<String, Integer>> getListBook() {
        ArrayList<HashMap<String, Integer>> alist = new ArrayList<HashMap<String, Integer>>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from "+BookmarksTable.TABLE_NAME, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
                    hashMap.put("surat", cursor.getInt(cursor.getColumnIndex("surat")));
                    hashMap.put("ayat", cursor.getInt(cursor.getColumnIndex("ayat")));
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

    private List<Books> getAllListBooks(){
        List<Books> books = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from "+BookmarksTable.TABLE_NAME+" order by "+BookmarksTable.FOLDER,null);
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Books book = new Books(cursor.getInt(1),
                            cursor.getInt(2),
                            cursor.getString(3));
                    books.add(book);
                }
            }
        }finally {
            closeCursor(cursor);
        }
        return books;
    }

    private void closeCursor(Cursor cursor){
        if(cursor!=null){
            try {
                cursor.close();
            } catch (Exception e) {
                // no op
            }
        }
    }

    static class BookmarksTable {
        static final String TABLE_NAME = "bookmarks";
        static final String ID = "_ID";
        static final String SURAT = "surat";
        static final String AYAT = "ayat";
        static final String FOLDER = "folder";
        static final String ISI = "isi";
        static final String ADDED_DATE = "tanggal";
    }

    public Observable<List<Books>> getListBooks(){
        Observable<List<Books>> observable = Observable.create(e -> {
            List<Books> books = getAllListBooks();
            e.onNext(books);
        });
        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
