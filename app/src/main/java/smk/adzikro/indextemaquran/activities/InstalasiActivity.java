package smk.adzikro.indextemaquran.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import smk.adzikro.indextemaquran.adapter.BannerAdapter;
import smk.adzikro.indextemaquran.adapter.ListLafdziAdapter;
import smk.adzikro.indextemaquran.constans.QuranConstants;
import smk.adzikro.indextemaquran.constans.QuranFileConstants;
import smk.adzikro.indextemaquran.db.CreateDb;
import smk.adzikro.indextemaquran.db.DatabaseHandler;
import smk.adzikro.indextemaquran.db.QuranDatabase;
import smk.adzikro.indextemaquran.object.VerseRange;
import smk.adzikro.indextemaquran.services.QuranDownloadService;
import smk.adzikro.indextemaquran.services.utils.ServiceIntentHelper;
import smk.adzikro.indextemaquran.setting.QuranSettings;
import smk.adzikro.indextemaquran.R;
import smk.adzikro.indextemaquran.constans.Constants;
import smk.adzikro.indextemaquran.db.ExtractDatabase;
import smk.adzikro.indextemaquran.util.Fungsi;
import smk.adzikro.indextemaquran.util.QuranFileUtils;
import smk.adzikro.indextemaquran.widgets.GridAutofitLayoutManager;

/**
 * Created by server on 11/26/16.
 */

public class InstalasiActivity extends AppCompatActivity implements
        View.OnClickListener{


    String TAG="InstalasiActivity";
    final private int WRITE_ESCDARD=1;
    QuranSettings settings;
    Button finish;
    ProgressBar progressBar;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags
                (WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_instalasi);
        finish = findViewById(R.id.finish);
        progressBar = findViewById(R.id.progress);
        if(Fungsi.isPertama(InstalasiActivity.this)) {
            new ExecuteSync().execute();
        }else{
            startActivity(new Intent(InstalasiActivity.this, MainActivity.class));
            finish();
        }
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                new ExecuteSync().execute();
              //  Fungsi.PATH_DATABASES(InstalasiActivity.this);
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void pertama(){
        settings = QuranSettings.getInstance(this);
        if(!Fungsi.isPertama(this)){
            startActivity(new Intent(InstalasiActivity.this,MainActivity.class));
            finish();
        }else {
            setContentView(R.layout.activity_instalasi);
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "checkSelfPermission ");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.e(TAG, "show request permision ");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_ESCDARD);
                } else {

                    // No explanation needed, we can request the permission.
                    Log.e(TAG, "tidak butuh show request permision ");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_ESCDARD);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                Log.e(TAG, "cek self fermision false");
                if (!Fungsi.isPertama(this)) {
                    new ExecuteSync().execute();
                }
            }

        }
    }



    private ArrayList<HashMap<String, String>> getData(){
                ArrayList<HashMap<String, String>> hasil = new ArrayList<>();
                DatabaseHandler db=DatabaseHandler.getDatabaseHandler(InstalasiActivity.this, QuranFileConstants.ARABIC_DATABASE);
                VerseRange verseRange = new VerseRange(1,1,1,7);
                List<String> arab = db.getLafadzArabic(verseRange,DatabaseHandler.TextType.ARABIC);
                db=DatabaseHandler.getDatabaseHandler(InstalasiActivity.this, QuranFileConstants.LATIN_DATABASE);
                List<String> arti = db.getLafadzArabic(verseRange,DatabaseHandler.TextType.TRANSLATION);
                for (int i=0; i<arab.size();i++){
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("arab",arab.get(i));
                    hashMap.put("arti",arti.get(i));
                    hasil.add(hashMap);
                }
                return hasil;
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    aset_quran = true;
                    break;
                case 1:
                    tema = true;
                    break;
                case 2:
                    latin = true;
                    break;
                case 3:
                    break;
                case 4:
                    aset_words = true;
                    break;
                case 5:
                    finish();
                    break;
        }
            super.handleMessage(msg);
        }
    };
    private void donwloadImage(){
        if(!Fungsi.isFileImageExist(this)) {
            settings.setDowloadImage(true);
         //   new DownloadZipfromInternet().execute("https://www.dropbox.com/s/5xgul9c98bcgzv3/quran.zip?dl=1");
        }
    }
    boolean aset_quran=false;
    boolean aset_words=false;
    boolean tema=false;
    boolean latin=false;

    private void extratData(){
        Fungsi.createPolder(InstalasiActivity.this);
        copyAssets();
        copyTema();
        createTableLatin();
    }
    private void donlot(){
        Toast.makeText(this,"Hayu donlot",Toast.LENGTH_SHORT).show();
        String notificationTitle = "Notif donlot";
        String url = QuranFileUtils.getArabicSearchDatabaseUrl();
        String destination = Fungsi.PATH_DATABASE();
        Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
                destination, notificationTitle, Constants.TRANSLATION_DOWNLOAD_KEY,
                QuranDownloadService.DOWNLOAD_TYPE_TRANSLATION);
        String filename = QuranFileConstants.ARABIC_DATABASE;
        if (url.endsWith("zip")) {
            filename += ".zip";
        }
        intent.putExtra(QuranDownloadService.EXTRA_OUTPUT_FILE_NAME, filename);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_ESCDARD: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(Fungsi.isPertama(this)) {
                       extratData();
                    }
                } else {
                    Log.e(TAG,"ditolak :(");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //   setPertama(true);
                 //   this.finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void insertTema(SQLiteDatabase db,String _id, String parent, String tema) {
        ContentValues qr = new ContentValues();
        qr.put(QuranDatabase.FIELD_ID_, _id);
        qr.put(QuranDatabase.FIELD_PARENT, parent);
        qr.put(QuranDatabase.FIELD_TEMA, tema);
        db.insert(QuranDatabase.TABLE_TEMA, null, qr);
    }
    public void insertAyatTema(SQLiteDatabase db, String id, String surat, String ayat) {
        ContentValues qr = new ContentValues();
        qr.put(QuranDatabase.FIELD_ID, id);
        qr.put(QuranDatabase.FIELD_SURAT, surat);
        qr.put(QuranDatabase.FIELD_AYAT, ayat);
        db.insert(QuranDatabase.TABLE_AYAT_TEMA, null, qr);
    }

    private void createTableLatin(){
        File file = new File(Fungsi.PATH_DATABASES(InstalasiActivity.this)+File.separator+QuranFileConstants.LATIN_DATABASE);
        if (file.exists()){
            handler.sendMessage(Message.obtain(handler,2));
            return;
        }
           Log.e(TAG, "Craete Table Latin");
        Resources res = getResources();
        InputStream inputStream = null;
        BufferedReader buff = null;
        SQLiteDatabase db = new CreateDb(InstalasiActivity.this, QuranFileConstants.LATIN_DATABASE).getWritableDatabase();
        String sql = "INSERT INTO "+CreateDb.TABLE_NAME+"("+
           CreateDb.FIELD_SURAT+","+CreateDb.FIELD_AYAT+","+CreateDb.FIELD_TEXT+") VALUES(?,?,?)";
        db.beginTransaction();
        SQLiteStatement query = db.compileStatement(sql);
        try{
            for(int surat=1; surat<=114; surat++) {
           inputStream = res.getAssets().open("latin/f"+surat+".dat");
           buff = new BufferedReader(new InputStreamReader(inputStream));
           String line;
           int ayat=1;
           while ((line = buff.readLine())!=null){
               query.bindLong(1, surat);
               query.bindLong(2, ayat);
               query.bindString(3, line);
               query.execute();
               query.clearBindings();
               ayat++;
           }

       }
       db.setTransactionSuccessful();
       db.endTransaction();
       db.close();
            Log.e(TAG, "Selsesai cretae Table Latin");
       handler.sendMessage(Message.obtain(handler,2));
   }catch (IOException e){

   }
  }

    private void copyTema() {
        File file = new File(Fungsi.PATH_DATABASES(InstalasiActivity.this)+ "/index4");
        if (file.exists()){
            handler.sendMessage(Message.obtain(handler,1));
            return;
        }
            Log.e(TAG, "Copy Tema");
            QuranDatabase dbx = new QuranDatabase(InstalasiActivity.this);
            SQLiteDatabase database = dbx.pembukaDatabase.getWritableDatabase();
            final Resources res = getResources();
            InputStream inputTema = res.openRawResource(R.raw.tema);
            InputStream inputAyatTema = res.openRawResource(R.raw.ayat_tema);
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(inputTema));
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(inputAyatTema));
            database.beginTransaction();
            try {
                String line;
                while ((line = reader1.readLine()) != null) {
                    String[] kata = TextUtils.split(line, ";");
                    if (kata.length < 3) continue;
                    insertTema(database,kata[0].trim(), kata[1].trim(), kata[2].trim());
                }
                while ((line = reader2.readLine()) != null) {
                    String[] kata = TextUtils.split(line, ";");
                    if (kata.length < 3) continue;
                    insertAyatTema(database,kata[0].trim(), kata[1].trim(), kata[2].trim());
                }
                database.setTransactionSuccessful();
                database.endTransaction();
                database.close();
                handler.sendMessage(Message.obtain(handler,1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "Selesai.. Copy Tema");
    }











    @Override
    public void onClick(View view) {


    }

    private void copyAssets() {
        Fungsi.PATH_DATABASES(InstalasiActivity.this);
            Log.e(TAG, "Copy Assets");
            AssetManager assetManager = getAssets();
            String[] files = null;
            try {
                files = assetManager.list("dba");
            } catch (IOException e) {
                Log.e(TAG, "Error IO copy dba "+e.getMessage());
            }
            for(String filename: files) {
                File filex = new File(Fungsi.PATH_DATABASES(InstalasiActivity.this)+File.separator+filename);
                if(filex.exists()){
                    if (filename.equals("words.db")) {
                        aset_words = true;
                    }
                    if (filename.equals("quran.ar.db")) {
                        aset_quran = true;
                    }
                    continue;
                }
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open("dba/" + filename);
                    out = new FileOutputStream(Fungsi.PATH_DATABASES(InstalasiActivity.this) +File.separator+ filename);
                    Log.e(TAG,"copy "+Fungsi.PATH_DATABASES(InstalasiActivity.this) +File.separator+ filename);
                    copyFile(in, out);
                    in.close();
                    out.flush();
                    out.close();
                    if (filename.equals("words.db")) {
                        aset_words = true;
                    }
                    if (filename.equals("quran.db")) {
                        aset_quran = true;
                        File file = new File(Fungsi.PATH_DATABASES(InstalasiActivity.this)+File.separator+"quran.db");
                        if(file.exists()){
                            file.renameTo(new File(Fungsi.PATH_DATABASES(InstalasiActivity.this)+File.separator+"quran.ar.db"));
                        }
                    }
                    Log.e(TAG, "Selesai Copy Assets");
                } catch (Exception e) {
                    Log.e(TAG, "aya eror "+e.getMessage());
                }
            }


    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }


    public void insertInto(SQLiteDatabase db, String text, int ayat,int surat) {
        ContentValues qr = new ContentValues();
        qr.put(CreateDb.FIELD_SURAT, surat);
        qr.put(CreateDb.FIELD_AYAT, ayat);
        qr.put(CreateDb.FIELD_TEXT, text);
        db.insertWithOnConflict(CreateDb.TABLE_NAME,null,qr,SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void createDb(){
        new Thread(new Runnable() {
            @Override
            public void run() {
               // QuranDatabase dbx = new QuranDatabase(InstalasiActivity.this);
                SQLiteDatabase dbAsal = new CreateDb(InstalasiActivity.this, "index3").getWritableDatabase();//dbx.pembukaDatabase.getWritableDatabase();
                //SQLiteDatabase dbAsal = QuranDatabase.open(InstalasiActivity.this);
                //ibnu Katsir
                SQLiteDatabase db = new CreateDb(InstalasiActivity.this, "quran.id.adzikro.db").getWritableDatabase();
                Cursor cursor = dbAsal.rawQuery("select * from  tafsir_adzikro", null);

                db.beginTransaction();
                  if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                     insertInto(db,cursor.getString(3),cursor.getInt(2),cursor.getInt(1));
                    }
                }
                cursor.close();
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
                handler.sendMessage(Message.obtain(handler,5));
                /*
                // irab
                db = new CreateDb(InstalasiActivity.this, "quran.ar.irab.db").getWritableDatabase();
                cursor = dbAsal.rawQuery("select * from  nahwusharf", null);
                db.beginTransaction();
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        insertInto(db,cursor.getString(3),cursor.getInt(2),cursor.getInt(1));
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();

                // sharaf
                db = new CreateDb(InstalasiActivity.this, "quran.ar.sharf.db").getWritableDatabase();
                cursor = dbAsal.rawQuery("select * from  nahwusharf", null);
                db.beginTransaction();
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        insertInto(db,cursor.getString(4),cursor.getInt(2),cursor.getInt(1));
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();

                // balagha
                db = new CreateDb(InstalasiActivity.this, "quran.ar.balagha.db").getWritableDatabase();
                cursor = dbAsal.rawQuery("select * from  nahwusharf", null);
                db.beginTransaction();
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        insertInto(db,cursor.getString(5),cursor.getInt(2),cursor.getInt(1));
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
                handler.sendMessage(Message.obtain(handler,5)); */
            }
        }).start();
    }

    class ExecuteSync extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            extratData();
            return "Execute";
        }
        @Override
        protected void onPostExecute(String re){
            Log.e(TAG, "Selesai semua ");
            progressBar.setVisibility(View.GONE);
            Fungsi.setPertama(InstalasiActivity.this, false);
            startActivity(new Intent(InstalasiActivity.this, MainActivity.class ));
            finish();
      }
    }
}
