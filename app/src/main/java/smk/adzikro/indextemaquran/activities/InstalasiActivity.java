package smk.adzikro.indextemaquran.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
//import net.sqlcipher.database.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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
    private ViewPager viewPager;
    private TypedArray mBannerArray;
    private int numberOfBannerImage;
    private View[] mBannerDotViews;
    private LinearLayout mBannerDotsLayout;
    private BannerAdapter adapter;
    private Button finish;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags
                (WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_instalasi);
     //   createDb();
        pertama();
        viewPager = findViewById(R.id.bannerViewPager);
        finish =findViewById(R.id.finish);

        mBannerArray = getResources().obtainTypedArray(R.array.image);
        numberOfBannerImage = mBannerArray.length();
        mBannerDotViews = new View[numberOfBannerImage];
        mBannerDotsLayout = findViewById(R.id.bannerDotsLayout);
        adapter = new BannerAdapter(this, mBannerArray);
        viewPager.setAdapter(adapter);
        for(int i=0; i < numberOfBannerImage; i++){
            final View bannerDot = new View(this);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            param.height = getResources().getDimensionPixelSize(R.dimen.stand10);
            param.width= getResources().getDimensionPixelSize(R.dimen.stand10);
            param.setMargins(getResources().getDimensionPixelSize(R.dimen.stand08),0,0,0);
            bannerDot.setLayoutParams(param);
            bannerDot.setBackgroundResource(R.drawable.shape_deselected_dot);
            mBannerDotsLayout.addView(bannerDot);
            mBannerDotViews[i] = bannerDot;
        }

        AutoSwipeBaner();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeDotBG(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (!false) {
         flags |= View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        viewPager.setSystemUiVisibility(flags);
        finish.setOnClickListener(view -> {
            if(finish.getText().equals(R.string.next)){
                int i=viewPager.getCurrentItem();
                viewPager.setCurrentItem(i+1);
            }else{
                handler.sendMessage(Message.obtain(handler,3));
            }
        });
    }
    private Timer swipeTimer;
    private void AutoSwipeBaner(){
        final Handler hand = new Handler();
        final Runnable update = () -> {
            int cuurentPage = viewPager.getCurrentItem();
            if(aset_quran && tema && latin && aset_words){
                finish.setText(R.string.next);
                if(cuurentPage==numberOfBannerImage-1){
                    finish.setText(R.string.finish);
                    handler.sendMessage(Message.obtain(handler,3));
                }
            }
            if(cuurentPage==numberOfBannerImage-1){
                cuurentPage =-1;
            }

            viewPager.setCurrentItem(cuurentPage+1,true);

        };
        swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                hand.post(update);
            }
        },500,3000);
    }
    @Override
    public void onDestroy(){
        if(swipeTimer!=null){
            swipeTimer=null;
        }
        super.onDestroy();
    }
    private void changeDotBG(int position){

        for(int i = 0; i < numberOfBannerImage; i++){
            if(position==i){
                mBannerDotViews[i].setBackgroundResource(R.drawable.shape_selected_dot);
            }else{
                mBannerDotViews[i].setBackgroundResource(R.drawable.shape_deselected_dot);
            }

        }
    }
    private void pertama(){
        settings = QuranSettings.getInstance(this);
        if(Fungsi.getInstalasi(this)){
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
                if (!Fungsi.getInstalasi(this)) {
                    extratData();
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
                    if(swipeTimer!=null){
                        swipeTimer=null;
                    }
                    startActivity(new Intent(InstalasiActivity.this,MainActivity.class));
                    Fungsi.setInstalasi(InstalasiActivity.this,true);
                    finish();
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
        if(!Fungsi.isFileImageExist()) {
            settings.setDowloadImage(true);
         //   new DownloadZipfromInternet().execute("https://www.dropbox.com/s/5xgul9c98bcgzv3/quran.zip?dl=1");
        }
    }
    boolean aset_quran=false;
    boolean aset_words=false;
    boolean tema=false;
    boolean latin=false;

    private void extratData(){
        Fungsi.createPolder();
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
                    if(!Fungsi.getInstalasi(this)) {
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
        File file = new File(Fungsi.PATH_DATABASE()+ QuranFileConstants.LATIN_DATABASE);
        if (file.exists()){
            handler.sendMessage(Message.obtain(handler,2));
            return;
        }
       new Thread(() -> {
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
       handler.sendMessage(Message.obtain(handler,2));
   }catch (IOException e){

   }
       }).start();
    }

    private void copyTema() {
        File file = new File(Fungsi.PATH_DATABASE()+ "index4");
        if (file.exists()){
            handler.sendMessage(Message.obtain(handler,1));
            return;
        }
        Runnable runnable = () -> {
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
        };
        new Thread(runnable).start();
    }











    @Override
    public void onClick(View view) {


    }

    private void copyAssets() {
        new Thread(() -> {
            AssetManager assetManager = getAssets();
            String[] files = null;
            try {
                files = assetManager.list("dba");
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }
            for(String filename: files) {
                File filex = new File(Fungsi.PATH_DATABASE()+filename);
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
                    out = new FileOutputStream(Fungsi.PATH_DATABASE() + filename);
                    Log.e(TAG,"copy "+Fungsi.PATH_DATABASE() + filename);
                    copyFile(in, out);
                    in.close();
                    out.flush();
                    out.close();
                    if (filename.equals("words.db")) {
                        aset_words = true;
                    }
                    if (filename.equals("quran.db")) {
                        aset_quran = true;
                        File file = new File(Fungsi.PATH_DATABASE()+"quran.db");
                        if(file.exists()){
                            file.renameTo(new File(Fungsi.PATH_DATABASE()+"quran.ar.db"));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "aya eror "+e.getMessage());
                }
            }

        }).start();
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


}
