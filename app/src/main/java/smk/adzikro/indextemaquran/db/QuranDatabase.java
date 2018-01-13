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

import smk.adzikro.indextemaquran.util.Fungsi;

/**
 * Created by server on 11/20/15.
 */

public class QuranDatabase {
    private static final String TAG = "QuranDatabase";

    //TABLE UNTUK PENCARIAN TABLE FTS3
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
    public static final String TABLE_TERJEMAH = "TERJEMAH_LAFDZI";
    public static final String TABLE_AYAT_TEMA = "AYAT_TEMA";
    public static final String TABLE_KALIMAH = "KALIMAH";
    //TABLE AYAT_TEMA
    private static String DB_PATH ;
    //private static final String NAMA_DATABASE = Fungsi.PATH_DATABASE()+"index3.dat";
    private static final String NAMA_DATABASE = Fungsi.PATH_DATABASE()+"index3";

    private static final int VERSI_DATABASE = 3;
    // perantara antara database dan aplikasi
    private final QuranOpenHelper pembukaDatabase;
    private static SQLiteDatabase db;
    private  final Context bantuBukaDB ;


    private static final HashMap<String,String> penghubungKolom = buatPenghubungKolom();


    public QuranDatabase(Context context) {
        this.bantuBukaDB =context;
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
            String myPath = DB_PATH + NAMA_DATABASE;
           // db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
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
      //  db = new QuranOpenHelper(bantuBukaDB).getReadableDatabase(Fungsi.getKunci());
        Cursor c = db.rawQuery("SELECT * FROM ALQURAN WHERE surat=1", null);
        // Note: Master is the one table in External db. Here we trying to access the records of table from external db.
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

    private static final String TABLE_BOOK="create table IF NOT EXISTS book(_id int, page int, aksi int, ket text, judul text, surat int, ayat int)";


    // ini utk membuat/membuka databasenya.CLAss turunan

    public static class QuranOpenHelper extends SQLiteOpenHelper {



        QuranOpenHelper(Context bantuBukaQuran) {
            super(bantuBukaQuran, NAMA_DATABASE, null, VERSI_DATABASE);
            //SQLiteDatabase.loadLibs(bantuBukaQuran);
            DB_PATH = Environment.getExternalStorageDirectory()
                    .getAbsolutePath().toString()+"/.adzikro/indexQuran/database/";

        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(FTS_TABLE_QURAN_CREATE);
            db.execSQL(TABLE_TEMA_CREATE);
            db.execSQL(AYAT_TEMA_CREATE);
            db.execSQL(TABLE_BOOK);
            db.execSQL("CREATE TABLE IF NOT EXISTS lafdzi (_id int, SURAT int, AYAT int, KATAARAB text, ARTI text)");
            db.execSQL("CREATE TABLE IF NOT EXISTS tafsir_adzikro (_id int, SURAT int, AYAT int, TAFSIR text)");
            db.execSQL("CREATE TABLE IF NOT EXISTS tafsir_ibnu_katsir (_id int, SURAT int, AYAT int, TAFSIR text)");
            db.execSQL("CREATE TABLE IF NOT EXISTS tafsir_jalalain (_id int, SURAT int, AYAT int, TAFSIR text)");
            db.execSQL("CREATE TABLE IF NOT EXISTS nahwusharf (_id int, SURAT int, AYAT int, irab text, sharf text,balagha text,fawaed text)");
            db.execSQL("CREATE TABLE IF NOT EXISTS tafsir_inggris (_id int, SURAT int, AYAT int, TAFSIR text)");
            db.execSQL("CREATE TABLE IF NOT EXISTS ulum (_id int, judul text, isi text)");
            db.execSQL("CREATE TABLE IF NOT EXISTS ALQURAN(_id int auto_increment, surat int, ayat int, gundul text, arab text, ind text)");
            db.execSQL("DROP TABLE IF EXISTS book");
            db.execSQL("CREATE TABLE IF NOT EXISTS book(_id int auto_increment, aksi int, surat int, ayat int)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase gantiDataBase, int versiLama, int versiBaru) {
        }

    }

    public static SQLiteDatabase open(Context context) throws SQLException{
        String myPath = NAMA_DATABASE;
        db = new QuranOpenHelper(context).getWritableDatabase();
        return db;
    }
    public SQLiteDatabase getRead() throws SQLException{
        //db =pembukaDatabase.getReadableDatabase();
        String myPath = NAMA_DATABASE;
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
      //  db = new QuranOpenHelper(bantuBukaDB).getReadableDatabase(Fungsi.getKunci());
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

    public void addBook(int page, int aksi, String keterangan, String judul, int surat, int ayat) {
        Cursor c = db.rawQuery("select _id from book",null);
        int jml =c.getCount();
        if (jml==0){
            jml=1;}else{
            jml++;
        }
        ContentValues qr = new ContentValues();
        qr.put("_id", jml);
        qr.put("page", page);
        qr.put("aksi", aksi);
        qr.put("ket", keterangan);
        qr.put("judul", judul);
        qr.put("surat", surat);
        qr.put("ayat", ayat);
        db.insertWithOnConflict("book", null, qr, SQLiteDatabase.CONFLICT_IGNORE);
        c.close();
        //  db.execSQL("insert into book values(" + jml + "," + page + "," + aksi + ",\"" + keterangan + "\",\"" + judul + "\")");
    //    System.out.println(judul);
    }
    public Cursor getBookmark(){
        String query="select * from book";
        return db.rawQuery(query,null);
    }

    public void getHapus(int id){
        String query="delete from book where _id="+id;
        db.execSQL(query);
    }
    public Boolean getBook(int page, int aksi, int surat, int ayat){
        Cursor c;
        if (aksi==12){
                c = db.rawQuery("select * from book where page="+page, null);
            }else{
                c = db.rawQuery("select * from book where aksi=" + aksi + " and surat=" + surat + " and ayat=" + ayat, null);
            }
        c.moveToFirst();
        if(c.getCount()!=0) {
            c.close();
            return true;
        }else{
            c.close();
            return false;
        }
     }

    public static int getIdAyat(int surat, int ayat) {
        String query="select _id from ALQURAN where surat="+surat+" and ayat="+ayat;
        Cursor c=db.rawQuery(query, null);
        c.moveToFirst();
        int i =c.getInt(c.getColumnIndex("_id"));
        c.close();
       return i;
    }





    public void beginTransaksi(){
        db.beginTransaction();
    }

    public void setTransaksiSukses(){
        db.setTransactionSuccessful();
    }

    public void transaksiSelesai(){
        db.endTransaction();
    }

    public Cursor getQuran(){
        String query="select * from ALQURAN";
        return db.rawQuery(query, null);
    }

    public int getTema(){
        String query="select * from TEMA where id=10";
        Cursor c=db.rawQuery(query, null);
        return c.getCount();
    }
    public Cursor getTafsirAyat(int surat, int ayat){
        String q="select tadarrus._id, tadarrus.matan, tadarrus.arti, tafsir.tafsir from tadarrus, tafsir " +
                "where tadarrus.surah=tafsir.surat " +
                "and tadarrus.ayat=tafsir.ayat " +
                "and tafsir.surat=" +surat+
                " and tafsir.ayat="+ayat;
        return db.rawQuery(q,null);
    }
    public int getJumlahAyat(int noSurat){
        int jAyat=0;
        String query="select ayat from nama_surat where no="+noSurat;
        Cursor c=db.rawQuery(query, null);
        while (c.moveToNext()) {
           jAyat = c.getInt(0);
        }
        return jAyat;
    }
    public Cursor getNamaSurat(){
        String query="select _id, nama_surat ||' '|| arb as nama from nama_surat";
        return db.rawQuery(query, null);

    }
    public int getCountKalimah(){
        String query="select * from "+TABLE_KALIMAH;
        Cursor c=db.rawQuery(query, null);
        return c.getCount();
    }

    public Cursor getKalimah(int limit){
        int i;
        i=10*limit;
        String query="select * from "+TABLE_KALIMAH+" LIMIT "+i+", 10";
        return db.rawQuery(query, null);
    }

    public Cursor getKalimahInQuran(String kalimah){
        String query="select ALQURAN._id, ALQURAN.arab, ALQURAN.ind," +
                     " nama_surat.nama_surat||' '||nama_surat.no||':'||ALQURAN.ayat||' ('||nama_surat.arb ||')'  as surat from ALQURAN, nama_surat " +
                     " where ALQURAN.surat=nama_surat.no "+
                    " and ALQURAN.arab like '%"+kalimah+"%'";
        return db.rawQuery(query,null);
    }


    public void getInputDataQuran(){
    //    db =pembukaDatabase.getWritableDatabase();
        String query="insert into QURAN select gundul, ind, surat, ayat, arab  from ALQURAN";
        db.execSQL(query);
    }



    public void insertTema(String _id, String parent, String tema) {
        ContentValues qr = new ContentValues();
        qr.put(FIELD_ID_, _id);
        qr.put(FIELD_PARENT, parent);
        qr.put(FIELD_TEMA, tema);
        db.insert("TEMA", null, qr);
    }
    public void insertAyatTema( String id, String surat, String ayat) {
        ContentValues qr = new ContentValues();
        qr.put(FIELD_ID, id);
        qr.put(FIELD_SURAT, surat);
        qr.put(FIELD_AYAT, ayat);
        db.insert(TABLE_AYAT_TEMA, null, qr);
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
