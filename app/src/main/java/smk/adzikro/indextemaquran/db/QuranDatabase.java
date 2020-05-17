package smk.adzikro.indextemaquran.db;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
//import net.sqlcipher.database.SQLiteDatabase;
//import net.sqlcipher.database.SQLiteOpenHelper;
//import net.sqlcipher.database.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Environment;
import android.provider.BaseColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.database.SQLException;

import java.util.HashMap;

import smk.adzikro.indextemaquran.constans.QuranFileConstants;
import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * Created by server on 11/20/15.
 */

public class QuranDatabase {
    private static final String TAG = "QuranDatabase";

    public static final String FIELD_GUNDUL = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String FIELD_IND = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String FIELD_ARAB = "ARAB";
    public static final String FIELD_AYAT ="AYAT";
    public static final String FIELD_SURAT ="SURAT";
    public static final String TABLE_QURAN = "QURAN";
    public static final String FIELD_IND_ ="IND";
    public static final String FIELD_TAFSIR ="TAFSIR";
    public static final String FIELD_ARTI ="ARTI";
    public static final String TABLE_QURAN_TEMA = "ALQURAN";
    //TABLE TEMA
    public static final String FIELD_ID_ = "_id";
    public static final String FIELD_ID = "id";
    public static final String FIELD_TEMA = "TEMA";
    public static final String FIELD_PARENT = "PARENT";
    public static final String TABLE_TEMA = "TEMA";
    public static final String TABLE_AYAT_TEMA = "AYAT_TEMA";
    private static String DB_PATH ;
    private static String NAMA_DATABASE;
    private static final int VERSI_DATABASE = 4;
    public final QuranOpenHelper pembukaDatabase;
    private static SQLiteDatabase db;
    private  final Context bantuBukaDB ;


    private static final HashMap<String,String> penghubungKolom = buatPenghubungKolom();


    public QuranDatabase(Context context) {
        this.bantuBukaDB =context;
        NAMA_DATABASE = Fungsi.PATH_DATABASES(context)+ "/index4";
        pembukaDatabase = new QuranOpenHelper(context);

    }

    private static HashMap<String,String> buatPenghubungKolom() {
        HashMap<String,String> menghubungkan = new HashMap<String,String>();
        menghubungkan.put(FIELD_GUNDUL, FIELD_GUNDUL);
        menghubungkan.put(FIELD_ARAB, FIELD_ARAB);
        menghubungkan.put(FIELD_IND, FIELD_IND);
        menghubungkan.put(FIELD_AYAT, FIELD_AYAT);
        menghubungkan.put(FIELD_SURAT, FIELD_SURAT);
        menghubungkan.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        menghubungkan.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        menghubungkan.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return menghubungkan;
    }

    public Cursor getWord(String barisID, String[] kolom) {
        String pilihKata = "rowId = ?";
        String[] kolomTempatKata = new String[] {barisID};

        return query(pilihKata, kolomTempatKata, kolom);


    }


    public Cursor getWordMatches(String cariKata, String[] kolom) {
        String pilihKata = TABLE_QURAN+ " MATCH ?";
        String[] tempatPenampungKata = new String[] {cariKata+"*"};

        return query(pilihKata, tempatPenampungKata, kolom);


    }
    private Cursor query(String pilihan, String[] gudangKataKata, String[] kolom) {
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(TABLE_QURAN);
            builder.setProjectionMap(penghubungKolom);
            db = new QuranOpenHelper(bantuBukaDB).getReadableDatabase();
            Cursor cursor = builder.query(db,
                    kolom, pilihan, gudangKataKata, null, null, null);

            if (cursor == null) {
                return null;
            } else if (!cursor.moveToFirst()) {
                cursor.close();
                return null;
            }
            return cursor;


    }


    public Cursor getData() {
        String myPath = DB_PATH + NAMA_DATABASE;
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        Cursor c = db.rawQuery("SELECT * FROM ALQURAN WHERE surat=1", null);
        return c;
    }

    public static final String FTS_TABLE_QURAN_CREATE =
            "CREATE VIRTUAL TABLE IF NOT EXISTS " + TABLE_QURAN +
                    " USING fts3 (" +
                    FIELD_GUNDUL + " text," +
                    FIELD_IND + " text, "+
                    FIELD_SURAT + " int, " +
                    FIELD_AYAT + " int," +
                    FIELD_ARAB + " text )";

    public static final String TABLE_TEMA_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TEMA +
                    "(" +
                    FIELD_ID_ + " INTEGER primary key, " +
                    FIELD_PARENT + " int ," +
                    FIELD_TEMA + " text)";

    public static final String AYAT_TEMA_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_AYAT_TEMA +
            "(" + FIELD_ID_ + " INTEGER AUTO_INCREMENT primary key, " +
            FIELD_ID + " int," + FIELD_SURAT + " int," + FIELD_AYAT + " int)";

    public class QuranOpenHelper extends SQLiteOpenHelper {
        QuranOpenHelper(Context bantuBukaQuran) {
            super(bantuBukaQuran, NAMA_DATABASE, null, VERSI_DATABASE);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(FTS_TABLE_QURAN_CREATE);
            db.execSQL(TABLE_TEMA_CREATE);
            db.execSQL(AYAT_TEMA_CREATE);
         }

        @Override
        public void onUpgrade(SQLiteDatabase gantiDataBase, int versiLama, int versiBaru) {
        }

    }

    public SQLiteDatabase getRead() throws SQLException{
        String myPath = NAMA_DATABASE;
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        return db;
    }
    public QuranDatabase close(){
        if(db!=null) {
            db.close();
        }
        if(pembukaDatabase!=null) {
            pembukaDatabase.close();
        }
        return this;
    }
    public int getCountTema(){
        int i=0;
        String query="select * from TEMA LIMIT 10";
        Cursor c=db.rawQuery(query, null);
        i =c.getCount();
        c.close();
        return i;
    }




    public int getTema(){
        String query="select * from TEMA where id=10";
        Cursor c=db.rawQuery(query, null);
        return c.getCount();
    }


    private boolean checkDatabase(){
            File file = new File(DB_PATH+NAMA_DATABASE);
                return file.exists();
    }
    private void copyDatabase() throws IOException{
        try {
            InputStream inputStream = bantuBukaDB.getAssets().open(NAMA_DATABASE);
            OutputStream outputStream = new FileOutputStream(DB_PATH+NAMA_DATABASE);
            byte[] buffer= new byte[1024];
            int length;
            while ((length=inputStream.read(buffer))>0)
                outputStream.write(buffer,0,length);
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   public void prepareDatabase(){
       boolean dbExist=checkDatabase();
       if(dbExist){
        //do nothing
       }
       else{
           try {
               getRead();
           } catch (SQLException e) {
               e.printStackTrace();
           }
           try {
               this.copyDatabase();
           } catch (IOException e) {
               e.printStackTrace();
           }

       }
   }
}
